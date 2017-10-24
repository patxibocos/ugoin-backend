package models.entities

import java.util.Date

case class Track(id: Long = 0, started: Date, finished: Option[Date], user: Long)
