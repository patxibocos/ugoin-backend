package dto.out

import models.entities.FollowStatus.FollowStatus
import play.api.libs.json.{Json, OWrites}

object FollowerResponseDTO {

  implicit val followerDtoWrites: OWrites[FollowerResponse] = Json.writes[FollowerResponse]

  case class FollowerResponse(status: FollowStatus, name: String)

}
