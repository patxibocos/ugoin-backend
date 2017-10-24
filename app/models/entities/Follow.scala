package models.entities

import java.util.Date

import models.entities.FollowStatus.FollowStatus

case class Follow(id: Long = 0, follower: Long, following: Long, source: Long, status: FollowStatus = FollowStatus.Sent, created: Date = new Date())

object FollowStatus extends Enumeration {
  type FollowStatus = Value

  val Sent = Value("Sent")
  val Accepted = Value("Accepted")
  val Rejected = Value("Rejected")
}