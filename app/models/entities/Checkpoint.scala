package models.entities

import java.util.Date

case class Checkpoint(id: Long = 0, latitude: Double, longitude: Double, tracked: Date = new Date(), track: Long)
