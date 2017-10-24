package models.db

import javax.inject.Inject

import models.entities.Source
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class SourceDbRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private lazy val sources = TableQuery[SourceTable]

  val Telegram = 1
  val Twitter = 2

  def insert(source: Source): Future[Long] = db.run(sources returning sources.map(_.id) += source)

  def findById(id: Long): Future[Source] = db.run(sources.filter(_.id === id).result.head)

  private class SourceTable(tag: Tag) extends Table[Source](tag, "source") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def * = (id, name) <> (Source.tupled, Source.unapply)
  }

}