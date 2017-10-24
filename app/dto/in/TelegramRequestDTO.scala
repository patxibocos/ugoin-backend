package dto.in

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

object TelegramRequestDTO {

  case class TelegramRequest(update_id: Long, message: String, from: UserRequest, chat: UserRequest)

  case class UserRequest(id: Long, username: String)

  implicit val userRequestReads: Reads[UserRequest] = (
    (JsPath \ "id").read[Long] and
      (JsPath \ "username").read[String]
    ) (UserRequest.apply _)

  implicit val telegramRequestReads: Reads[TelegramRequest] = (
    (JsPath \ "update_id").read[Long] and
      (JsPath \ "message" \ "text").read[String] and
      (JsPath \ "message" \ "from").read[UserRequest] and
      (JsPath \ "message" \ "chat").read[UserRequest]
    ) (TelegramRequest.apply _)

}
