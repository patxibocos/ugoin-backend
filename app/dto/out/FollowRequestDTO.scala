package dto.out

import models.entities.FollowStatus.FollowStatus

case class FollowRequestDTO(id: Long, name: String, status: FollowStatus)
