package models.db

import java.util.Date
import javax.inject.Inject

import models.entities.FollowStatus.FollowStatus
import models.entities.{Follow, FollowStatus, User}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FollowDbRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, sourceDbRepository: SourceDbRepository, telegramUserDbRepository: TelegramUserDbRepository) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private lazy val follows = TableQuery[FollowTable]

  def q(x: FollowTable)(implicit follow: Follow) = x.followerId === follow.follower && x.followingId === follow.following && x.sourceId === follow.source

  def insert(follow: Follow): Future[Long] = db.run(follows returning follows.map(_.id) += follow)

  def insertFollow(implicit follow: Follow): Future[Boolean] = db.run(follows.filter(q).exists.result).flatMap {
    case false => db.run(follows += follow).map(_ => true)
    case true => Future.successful(false)
  }

  private def updateFollow(follow: Follow, status: FollowStatus): Future[Unit] = {
    val q = for {f <- follows if f.id === follow.id} yield f.status
    db.run(q.update(status)).map(_ => ())
  }

  def acceptFollow(follow: Follow): Future[Unit] = {
    updateFollow(follow, FollowStatus.Accepted)
  }

  def rejectFollow(follow: Follow): Future[Unit] = {
    updateFollow(follow, FollowStatus.Rejected)
  }

  def removeFollow(implicit follow: Follow): Future[Boolean] = db.run(follows.filter(q).delete).map(_ > 0)

  def findFollowById(id: Long): Future[Option[Follow]] = {
    db.run(follows.filter(_.id === id).result.headOption)
  }

  def findByIdAndFollowing(id: Long, following: User): Future[Option[Follow]] = {
    db.run(follows.filter(f => f.id === id && f.followingId === following.id).result.headOption)
  }

  private implicit val followStatusMapper = MappedColumnType.base[FollowStatus, String](
    e => e.toString,
    s => FollowStatus.withName(s)
  )

  def getUserFollowers(userId: Long): Future[Seq[Follow]] = {
    db.run(follows.filter(_.followingId === userId).sortBy(_.created).result)
  }

  def getUserAcceptedFollowers(user: Long): Future[Seq[Follow]] = {
    db.run(follows.filter(f => f.followingId === user && f.status === FollowStatus.Accepted).sortBy(_.created).result)
  }

  private implicit val dateMapper = MappedColumnType.base[Date, java.sql.Date](
    d => new java.sql.Date(d.getTime),
    d => new Date(d.getTime)
  )

  private class FollowTable(tag: Tag) extends Table[Follow](tag, "follow") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def followerId = column[Long]("follower")

    def followingId = column[Long]("following")

    def sourceId = column[Long]("source")

    def status = column[FollowStatus]("status")

    def created = column[Date]("created")

    def * = (id, followerId, followingId, sourceId, status, created) <> (Follow.tupled, Follow.unapply)

  }

}