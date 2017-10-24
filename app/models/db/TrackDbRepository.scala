package models.db

import java.util.Date
import javax.inject.Inject

import models.entities.{Track, User}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TrackDbRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private lazy val tracks = TableQuery[TrackTable]

  private implicit val dateMapper = MappedColumnType.base[Date, java.sql.Date](
    d => new java.sql.Date(d.getTime),
    d => new Date(d.getTime)
  )

  def insert(track: Track): Future[Long] = db.run(tracks returning tracks.map(_.id) += track)

  def findById(id: Long): Future[Track] = db.run(tracks.filter(_.id === id).result.head)

  def countByUser(user: User): Future[Int] = db.run(tracks.filter(_.userId === user.id).length.result)

  def findByUser(user: User): Future[Seq[Track]] = db.run(tracks.filter(_.userId === user.id).result)

  def getCurrentActiveTrack(user: User): Future[Option[Track]] = {
    db.run(tracks.filter(t => t.userId === user.id && t.finished.isEmpty).sortBy(_.started.desc).result.headOption)
  }

  def endTrack(track: Track): Future[Unit] = {
    val q = for {t <- tracks if t.id === track.id} yield t.finished
    db.run(q.update(Some(new Date()))).map(_ => ())
  }

  private class TrackTable(tag: Tag) extends Table[Track](tag, "track") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def started = column[Date]("started")

    def finished = column[Option[Date]]("finished")

    def userId = column[Long]("user")

    def * = (id, started, finished, userId) <> (Track.tupled, Track.unapply)

  }

}