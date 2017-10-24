import java.util.Date

import base.BaseSpec
import models.db._
import models.entities._
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.mockito.MockitoSugar
import play.api.Application
import play.api.i18n.Lang
import play.api.libs.ws.WSResponse
import services.{TelegramService, TrackingService, TwitterService}

import scala.concurrent.Future

class TrackingServiceSpec extends BaseSpec with MockitoSugar {

  private def userDao(implicit app: Application) = Application.instanceCache[UserDbRepository].apply(app)

  private def trackDao(implicit app: Application) = Application.instanceCache[TrackDbRepository].apply(app)

  private def checkpointDao(implicit app: Application) = Application.instanceCache[CheckpointDbRepository].apply(app)

  private def sourceDao(implicit app: Application) = Application.instanceCache[SourceDbRepository].apply(app)

  private def telegramUserDao(implicit app: Application) = Application.instanceCache[TelegramUserDbRepository].apply(app)

  private def followDao(implicit app: Application) = Application.instanceCache[FollowDbRepository].apply(app)

  private val mockedTelegramService = mock[TelegramService]

  private val mockedTwitterService = mock[TwitterService]

  val trackingService = new TrackingService(trackDao, checkpointDao, followDao, mockedTelegramService, mockedTwitterService, sourceDao, telegramUserDao)

  "Tracking service" should {

    "creates a track and a checkpoint" in {
      val user = User(name = "test", email = None, password = "", salt = "")
      val latitude = 4.2
      val longitude = 2.5
      whenReady(userDao.insert(user)) { userId =>
        val savedUser = user.copy(id = userId)
        whenReady(trackingService.saveUserCheckpoint(savedUser, latitude, longitude)) { checkpointId =>
          whenReady(checkpointDao.findById(checkpointId)) { newCheckpoint =>
            newCheckpoint.latitude mustBe latitude
            newCheckpoint.longitude mustBe longitude
          }
          whenReady(trackDao.findByUser(savedUser)) { tracks =>
            tracks.length mustBe 1
            val track = tracks.head
            whenReady(checkpointDao.findByTrack(track)) { checkpoints =>
              checkpoints.length mustBe 1
            }
          }
        }
      }
    }

    "checkpoint is added to the existing track" in {
      val user = User(name = "test", email = None, password = "", salt = "")
      whenReady(userDao.insert(user)) { userId =>
        trackDao.insert(Track(started = new Date(), finished = None, user = userId))
        val savedUser = user.copy(id = userId)
        whenReady(trackingService.saveUserCheckpoint(savedUser, 1, 1)) { _ =>
          whenReady(trackDao.findByUser(savedUser)) { tracks =>
            tracks.length mustBe 1
          }
        }
      }
    }

    "checkpoint is added to a new track" in {
      val user = User(name = "test", email = None, password = "", salt = "")
      whenReady(userDao.insert(user)) { userId =>
        trackDao.insert(Track(started = new Date(), finished = Some(new Date()), user = userId))
        val savedUser = user.copy(id = userId)
        whenReady(trackingService.saveUserCheckpoint(savedUser, 1, 1)) { _ =>
          whenReady(trackDao.findByUser(savedUser)) { tracks =>
            tracks.length mustBe 2
          }
        }
      }
    }

    "notify to followers" in {
      val (latitude, longitude) = (4, 2)
      val userName = "test"
      val user = User(name = userName, email = None, password = "", salt = "")
      val telegramUser = TelegramUser(chatId = 1, username = "follower")
      whenReady(userDao.insert(user)) { userId =>
        whenReady(telegramUserDao.insert(telegramUser)) { telegramUserId =>
          val follow = Follow(1, telegramUserId, userId, sourceDao.Telegram, FollowStatus.Accepted)
          whenReady(followDao.insert(follow)) { _ =>
            when(mockedTelegramService.sendLocation(Seq(telegramUserId), userName, latitude, longitude)(Lang.defaultLang)).thenReturn(Future.successful(Seq.empty[WSResponse]))
            val savedUser = user.copy(id = userId)
            whenReady(trackingService.propagateLocationToFollowers(savedUser, latitude, longitude)(Lang.defaultLang)) { _ =>
              verify(mockedTelegramService, times(1)).sendLocation(Seq(telegramUserId), savedUser.name, latitude, longitude)(Lang.defaultLang)
            }
          }
        }
      }
    }

  }

}
