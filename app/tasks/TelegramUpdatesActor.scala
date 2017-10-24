package tasks

import javax.inject.{Named, Singleton}

import akka.actor.{Actor, ActorRef}
import com.google.inject.Inject
import models.db.SourceDbRepository
import models.entities.SourceMessage
import play.api.libs.json.{JsObject, Json}
import services.{SourceMessageService, TelegramService}

@Singleton
class TelegramUpdatesActor @Inject()(telegramService: TelegramService, sourceMessageService: SourceMessageService, sourceDbRepository: SourceDbRepository, @Named("telegram-actor") val telegramActor: ActorRef) extends Actor {

  override def receive: Receive = {
    case _ => sourceMessageService.consumeNewMessagesFromSource(
      sourceDbRepository.Telegram,
      telegramService.getUpdates,
      response => (Json.parse(response.body) \ "result").as[List[JsObject]].map(
        jsObject => SourceMessage(
          sourceMessageId = jsObject.value("update_id").as[Long],
          raw = jsObject.toString,
          source = sourceDbRepository.Telegram)),
      telegramActor)
  }

}
