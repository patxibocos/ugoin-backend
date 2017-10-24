package models.entities

case class SourceMessage(id: Long = 0, processed: Boolean = false, sourceMessageId: Long, raw: String, source: Long)
