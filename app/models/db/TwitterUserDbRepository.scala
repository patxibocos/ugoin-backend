package models.db

import javax.inject.Inject

import models.entities.TwitterUser
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class TwitterUserDbRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private lazy val twitterUsers = TableQuery[TwitterUserTable]

  def insert(twitterUser: TwitterUser): Future[Long] = db.run(twitterUsers returning twitterUsers.map(_.id) += twitterUser)

  def findByUserId(userId: Long): Future[Option[TwitterUser]] = db.run(twitterUsers.filter(_.userId === userId).result.headOption)

  def findByFollowerIds(ids: Seq[Long]): Future[Seq[TwitterUser]] = db.run(twitterUsers.filter(_.id inSet ids).result)

  private class TwitterUserTable(tag: Tag) extends Table[TwitterUser](tag, "twitter_user") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[Long]("user_id")

    def userScreenName = column[String]("user_screen_name")

    override def * = (id, userId, userScreenName) <> (TwitterUser.tupled, TwitterUser.unapply)
  }

}
