package services

import javax.inject.{Inject, Singleton}

import models.SocialCommand.{Follow, Unfollow}
import models.db.{FollowDbRepository, SourceDbRepository, UserDbRepository}
import models.{SocialCommand, entities}
import play.api.i18n.{Lang, MessagesApi}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SocialService @Inject()(userDbRepository: UserDbRepository, followDbRepository: FollowDbRepository, sourceDbRepository: SourceDbRepository, messagesApi: MessagesApi) {

  def follow(follower: Long, username: String, source: Long)(implicit lang: Lang): Future[String] = {
    for {
      optionUser <- userDbRepository.findByName(username)
      messageKey <- optionUser match {
        case Some(user) => followDbRepository.insertFollow(entities.Follow(follower = follower, following = user.id, source = source)).map(if (_) "social.follow" else "social.already_following")
        case None => Future.successful("social.user_not_found")
      }
    } yield messagesApi(messageKey, username)
  }

  def unfollow(follower: Long, username: String, source: Long)(implicit lang: Lang): Future[String] = {
    for {
      optionUser <- userDbRepository.findByName(username)
      messageKey <- optionUser match {
        case Some(user) => followDbRepository.removeFollow(entities.Follow(follower = follower, following = user.id, source = source)).map(if (_) "social.unfollow" else "social.not_following")
        case None => Future.successful("social.user_not_found")
      }
    } yield messagesApi(messageKey, username)
  }

  def executeCommand(socialCommand: SocialCommand, socialUserId: Long, source: Long)(implicit lang: Lang): Future[String] = {
    socialCommand.command match {
      case Follow => follow(socialUserId, socialCommand.value, source)
      case Unfollow => unfollow(socialUserId, socialCommand.value, source)
    }
  }

}
