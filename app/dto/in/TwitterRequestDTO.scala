package dto.in

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

object TwitterRequestDTO {

  case class TwitterRequest(update_id: Long, text: String, senderScreenName: String, senderId: Long)

  implicit val telegramRequestReads: Reads[TwitterRequest] = (
    (JsPath \ "id").read[Long] and
      (JsPath \ "text").read[String] and
      (JsPath \ "sender_screen_name").read[String] and
      (JsPath \ "sender_id").read[Long]
    ) (TwitterRequest.apply _)

}
