package services

import java.util.Date
import javax.inject.{Inject, Singleton}

import models.db._
import models.entities.{Checkpoint, Track, User}
import play.api.i18n.Lang

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class TrackingService @Inject()(trackDbRepository: TrackDbRepository, checkpointDbRepository: CheckpointDbRepository, followDbRepository: FollowDbRepository, telegramService: TelegramService, twitterService: TwitterService, sourceDbRepository: SourceDbRepository, telegramUserDbRepository: TelegramUserDbRepository) {

  def saveUserCheckpoint(user: User, latitude: Double, longitude: Double): Future[Long] = {
    trackDbRepository.getCurrentActiveTrack(user).flatMap({ optionTrack =>
      (optionTrack match {
        case Some(t) => Future.successful(t.id)
        case None => createNewTrack(user)
      }).flatMap(trackId => checkpointDbRepository.insert(Checkpoint(latitude = latitude, longitude = longitude, track = trackId)))
    })
  }

  def propagateLocationToFollowers(user: User, latitude: Double, longitude: Double)(implicit lang: Lang): Future[Boolean] = {
    followDbRepository.getUserAcceptedFollowers(user.id).flatMap(followers => {
      Future.sequence(followers.groupBy(_.source).map {
        case (sourceDbRepository.Telegram, telegramFollowers) =>
          telegramService.sendLocation(telegramFollowers.map(_.follower), user.name, latitude, longitude)
        case (sourceDbRepository.Twitter, twitterFollowers) =>
          twitterService.sendLocation(twitterFollowers.map(_.follower), user.name, latitude, longitude)
        case (_, _) => Future.successful(Seq.empty)
      }).map(_.flatten.forall(_.status == 200))
    })
  }

  def createNewTrack(user: User): Future[Long] = {
    // End current then create a new one
    for {
      _ <- endCurrentTrack(user)
      trackId <- trackDbRepository.insert(Track(started = new Date(), finished = None, user = user.id))
    } yield trackId
  }

  def endCurrentTrack(user: User): Future[Unit] = {
    trackDbRepository.getCurrentActiveTrack(user).map(_.map(trackDbRepository.endTrack))
  }

}
