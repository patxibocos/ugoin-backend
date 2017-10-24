package tasks

import javax.inject.Singleton

import akka.actor.Actor
import com.google.inject.Inject
import dto.in.TwitterRequestDTO.TwitterRequest
import models.SocialCommand
import models.db.{SourceDbRepository, SourceMessageDbRepository}
import play.api.i18n.Lang
import play.api.libs.json.Json
import services.TwitterService

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TwitterActor @Inject()(twitterService: TwitterService, sourceMessageDbRepository: SourceMessageDbRepository, sourceDbRepository: SourceDbRepository) extends Actor {

  override def receive: Receive = {
    case messageId: Long => sourceMessageDbRepository.findBySourceMessageIdAndSource(messageId, sourceDbRepository.Twitter).map(_.get).map(sourceMessage => {
      val twitterMessageData = Json.parse(sourceMessage.raw).as[TwitterRequest]
      SocialCommand.processCommand(twitterMessageData.text, """#(\S+) (\S+)""".r) match {
        case Some(socialCommand) =>
          twitterService.executeCommand(socialCommand, twitterMessageData.senderScreenName, twitterMessageData.senderId)(Lang.defaultLang)
          sourceMessageDbRepository.markAsProcessed(sourceMessage)
        case _ =>
      }
    })
  }

}
