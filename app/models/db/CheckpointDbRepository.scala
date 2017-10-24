package models.db

import java.util.Date
import javax.inject.Inject

import models.entities.{Checkpoint, Track}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class CheckpointDbRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private lazy val checkpoints = TableQuery[CheckpointTable]

  private implicit val dateMapper = MappedColumnType.base[Date, java.sql.Date](
    d => new java.sql.Date(d.getTime),
    d => new Date(d.getTime)
  )

  def insert(checkpoint: Checkpoint): Future[Long] = db.run(checkpoints returning checkpoints.map(_.id) += checkpoint)

  def findById(id: Long): Future[Checkpoint] = db.run(checkpoints.filter(_.id === id).result.head)

  def findByTrack(track: Track): Future[Seq[Checkpoint]] = db.run(checkpoints.filter(_.trackId === track.id).result)

  private class CheckpointTable(tag: Tag) extends Table[Checkpoint](tag, "checkpoint") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def latitude = column[Double]("latitude")

    def longitude = column[Double]("longitude")

    def tracked = column[Date]("tracked")

    def trackId = column[Long]("track")

    def * = (id, latitude, longitude, tracked, trackId) <> (Checkpoint.tupled, Checkpoint.unapply)

  }

}