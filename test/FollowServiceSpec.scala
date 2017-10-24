import base.BaseSpec
import models.db._
import models.entities.{Follow, FollowStatus, User}
import play.api.Application
import services.FollowService

class FollowServiceSpec extends BaseSpec {

  def followDao(implicit app: Application) = Application.instanceCache[FollowDbRepository].apply(app)

  def userDao(implicit app: Application) = Application.instanceCache[UserDbRepository].apply(app)

  def sourceDao(implicit app: Application) = Application.instanceCache[SourceDbRepository].apply(app)

  def telegramUserDao(implicit app: Application) = Application.instanceCache[TelegramUserDbRepository].apply(app)

  def twitterUserDao(implicit app: Application) = Application.instanceCache[TwitterUserDbRepository].apply(app)

  val followService = new FollowService(followDao, sourceDao, telegramUserDao, twitterUserDao)

  "A follow request" should {

    "change status to accepted" in {
      whenReady(userDao.insert(User(name = "test", email = None, password = "", salt = ""))) { userId =>
        val follow = Follow(follower = 0, following = userId, source = sourceDao.Telegram)
        whenReady(followDao.insert(follow)) { newFollowId =>
          whenReady(followService.acceptFollower(follow.copy(id = newFollowId))) { _ =>
            whenReady(followDao.findFollowById(newFollowId)) { updatedFollow =>
              assert(updatedFollow.get.status == FollowStatus.Accepted)
            }
          }
        }
      }
    }

    "change status to rejected" in {
      whenReady(userDao.insert(User(name = "test", email = None, password = "", salt = ""))) { userId =>
        val follow = Follow(follower = 0, following = userId, source = sourceDao.Telegram)
        whenReady(followDao.insert(follow)) { newFollowId =>
          whenReady(followService.rejectFollower(follow.copy(id = newFollowId))) { _ =>
            whenReady(followDao.findFollowById(newFollowId)) { updatedFollow =>
              assert(updatedFollow.get.status == FollowStatus.Rejected)
            }
          }
        }
      }
    }

  }

}