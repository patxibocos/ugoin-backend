package dto.in

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class RegisterUserRequest(username: String, password: String)

object RegisterUserRequestDTO {

  implicit val registerUserRequestReads: Reads[RegisterUserRequest] = (
    (JsPath \ "username").read[String] and
      (JsPath \ "password").read[String]
    ) (RegisterUserRequest.apply _)

}
