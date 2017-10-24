package services

import javax.inject.{Inject, Singleton}

import akka.actor.ActorRef
import models.db.{SourceDbRepository, SourceMessageDbRepository}
import models.entities.SourceMessage
import play.api.libs.ws.WSResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SourceMessageService @Inject()(sourceDbRepository: SourceDbRepository, sourceMessageDbRepository: SourceMessageDbRepository) {

  def consumeNewMessagesFromSource(source: Long, messageRetriever: Long => Future[WSResponse], messageParser: WSResponse => List[SourceMessage], actor: ActorRef) = {
    sourceMessageDbRepository.getLatestForSource(source).map { latest =>
      messageRetriever(latest.getOrElse(0)).map { response =>
        messageParser(response).foreach { sourceMessage =>
          sourceMessageDbRepository.insert(sourceMessage).map { _ =>
            actor ! sourceMessage.sourceMessageId
          }
        }
      }
    }
  }

}
