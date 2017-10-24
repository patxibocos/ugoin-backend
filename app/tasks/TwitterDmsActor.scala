package tasks

import javax.inject.{Named, Singleton}

import akka.actor.{Actor, ActorRef}
import com.google.inject.Inject
import models.db.{SourceDbRepository, SourceMessageDbRepository}
import models.entities.SourceMessage
import play.api.libs.json.{JsObject, Json}
import services.{SourceMessageService, TwitterService}

@Singleton
class TwitterDmsActor @Inject()(sourceMessageService: SourceMessageService, twitterService: TwitterService, sourceMessageDbRepository: SourceMessageDbRepository, sourceDbRepository: SourceDbRepository, @Named("twitter-actor") val twitterActor: ActorRef) extends Actor {

  override def receive: Receive = {
    case _ => sourceMessageService.consumeNewMessagesFromSource(
      sourceDbRepository.Twitter,
      twitterService.getDms,
      response => Json.parse(response.body).as[List[JsObject]].map(
        jsObject => SourceMessage(
          sourceMessageId = jsObject.value("id").as[Long],
          raw = jsObject.toString,
          source = sourceDbRepository.Twitter)),
      twitterActor)
  }

}
