package models.db

import javax.inject.Inject

import models.entities.SourceMessage
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class SourceMessageDbRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private lazy val sourceMessages = TableQuery[SourceMessageTable]

  def insert(sourceMessage: SourceMessage): Future[Long] = db.run(sourceMessages returning sourceMessages.map(_.id) += sourceMessage)

  def findBySourceMessageIdAndSource(sourceMessageId: Long, source: Long): Future[Option[SourceMessage]] = db.run(sourceMessages.filter(s => s.sourceMessageId === sourceMessageId && s.sourceId === source).result.headOption)

  def markAsProcessed(sourceMessage: SourceMessage) = db.run((for {s <- sourceMessages if s.id === sourceMessage.id} yield s.processed).update(true))

  def getLatestForSource(source: Long) = db.run(sourceMessages.filter(_.sourceId === source).map(_.sourceMessageId).max.result)

  private class SourceMessageTable(tag: Tag) extends Table[SourceMessage](tag, "source_message") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def sourceMessageId = column[Long]("source_message_id")

    def raw = column[String]("raw")

    def processed = column[Boolean]("processed")

    def sourceId = column[Long]("source")

    def * = (id, processed, sourceMessageId, raw, sourceId) <> (SourceMessage.tupled, SourceMessage.unapply)
  }

}
