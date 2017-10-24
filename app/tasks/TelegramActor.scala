package tasks

import javax.inject.Singleton

import akka.actor.Actor
import com.google.inject.Inject
import dto.in.TelegramRequestDTO.TelegramRequest
import models.SocialCommand
import models.db.{SourceDbRepository, SourceMessageDbRepository}
import play.api.i18n.Lang
import play.api.libs.json.Json
import services.TelegramService

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TelegramActor @Inject()(telegramService: TelegramService, sourceMessageDbRepository: SourceMessageDbRepository, sourceDbRepository: SourceDbRepository) extends Actor {

  override def receive: Receive = {
    case messageId: Long => sourceMessageDbRepository.findBySourceMessageIdAndSource(messageId, sourceDbRepository.Telegram).map(_.get).map(sourceMessage => {
      val telegramMessageData = Json.parse(sourceMessage.raw).as[TelegramRequest]
      SocialCommand.processCommand(telegramMessageData.message, """/(\S+) (\S+)""".r) match {
        case Some(socialCommand) =>
          telegramService.executeCommand(socialCommand, telegramMessageData.chat, telegramMessageData.from)(Lang.defaultLang)
          sourceMessageDbRepository.markAsProcessed(sourceMessage)
        case _ =>
      }
    })
  }

}
