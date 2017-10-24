package services

import javax.inject.{Inject, Singleton}

import dto.out.FollowerResponseDTO.FollowerResponse
import models.db.{FollowDbRepository, SourceDbRepository, TelegramUserDbRepository, TwitterUserDbRepository}
import models.entities.{Follow, FollowStatus, User}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class FollowService @Inject()(followDbRepository: FollowDbRepository, sourceDbRepository: SourceDbRepository, telegramUserDbRepository: TelegramUserDbRepository, twitterUserDbRepository: TwitterUserDbRepository) {

  def acceptFollower(follow: Follow): Future[Unit] = {
    if (follow.status != FollowStatus.Accepted) followDbRepository.acceptFollow(follow) else Future.successful(())
  }

  def rejectFollower(follow: Follow): Future[Unit] = {
    if (follow.status != FollowStatus.Rejected) followDbRepository.rejectFollow(follow) else Future.successful(())
  }

  def getFollowers(user: User): Future[Seq[FollowerResponse]] = {
    followDbRepository.getUserFollowers(user.id).flatMap(followers => {
      Future.traverse(followers.groupBy(_.source)) { followsBySource =>
        followsBySource._1 match {
          case sourceDbRepository.Telegram =>
            telegramUserDbRepository.findByFollowerIds(followsBySource._2.map(_.following)).
              map(followsBySource._2.zip(_).map(r => FollowerResponse(r._1.status, r._2.username)))
          case sourceDbRepository.Twitter =>
            twitterUserDbRepository.findByFollowerIds(followsBySource._2.map(_.following)).
              map(followsBySource._2.zip(_).map(r => FollowerResponse(r._1.status, r._2.userScreenName)))
        }
      }
    }).map(_.toSeq.flatten)
  }

}
