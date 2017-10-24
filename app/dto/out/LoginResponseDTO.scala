package dto.out

import play.api.libs.json.{Json, OWrites}

object LoginResponseDTO {

  case class LoginResponse(token: String)

  implicit val loginResponseWrites: OWrites[LoginResponse] = Json.writes[LoginResponse]


}