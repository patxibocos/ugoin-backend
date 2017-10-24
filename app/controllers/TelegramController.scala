package controllers

import javax.inject._

import akka.actor.ActorRef
import dto.in.TelegramRequestDTO.TelegramRequest
import models.db.{SourceDbRepository, SourceMessageDbRepository}
import models.entities.SourceMessage
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class TelegramController @Inject()(sourceMessageDbRepository: SourceMessageDbRepository, sourceDbRepository: SourceDbRepository, @Named("telegram-actor") val telegramActor: ActorRef, cc: ControllerComponents) extends AbstractController(cc) {

  def telegram = Action.async(parse.json) { request: Request[JsValue] =>
    request.body.validate[TelegramRequest] match {
      case s: JsSuccess[TelegramRequest] =>
        sourceMessageDbRepository.findBySourceMessageIdAndSource(s.get.update_id, sourceDbRepository.Telegram).flatMap {
          case Some(_) => Future.successful(BadRequest)
          case None =>
            sourceMessageDbRepository.insert(SourceMessage(sourceMessageId = s.get.update_id, raw = request.body.toString, source = sourceDbRepository.Telegram)).map { _ =>
              telegramActor ! s.get.update_id
              Ok("")
            }
        }
      case e: JsError => Future.successful(BadRequest(JsError.toJson(e)))
    }
  }

}
