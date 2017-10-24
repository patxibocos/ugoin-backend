package models.db

import javax.inject.Inject

import models.entities.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class UserDbRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private lazy val users = TableQuery[UserTable]

  def insert(user: User): Future[Long] = db.run(users returning users.map(_.id) += user)

  def findById(id: Long): Future[User] = db.run(users.filter(_.id === id).result.head)

  def findByName(name: String): Future[Option[User]] = db.run(users.filter(_.name.toLowerCase === name.toLowerCase).result.headOption)

  def findByToken(token: String): Future[Option[User]] = db.run(users.filter(_.token === token).result.headOption)

  private class UserTable(tag: Tag) extends Table[User](tag, "user") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def token = column[String]("token")

    def name = column[String]("name")

    def email = column[Option[String]]("email")

    def password = column[String]("password")

    def salt = column[String]("salt")

    override def * = (id, token, name, email, password, salt) <> (User.tupled, User.unapply)

  }

}