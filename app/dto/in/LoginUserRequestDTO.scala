package dto.in

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class LoginUserRequest(username: String, password: String)

object LoginUserRequestDTO {

  implicit val loginUserRequestReads: Reads[LoginUserRequest] = (
    (JsPath \ "username").read[String] and
      (JsPath \ "password").read[String]
    ) (LoginUserRequest.apply _)

}
