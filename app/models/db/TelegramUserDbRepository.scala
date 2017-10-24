package models.db

import javax.inject.Inject

import models.entities.TelegramUser
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class TelegramUserDbRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private lazy val telegramUsers = TableQuery[TelegramUserTable]

  def insert(telegramUser: TelegramUser): Future[Long] = db.run(telegramUsers returning telegramUsers.map(_.id) += telegramUser)

  def findById(id: Long): Future[Option[TelegramUser]] = db.run(telegramUsers.filter(_.id === id).result.headOption)

  def findByChatId(chatId: Long): Future[Option[TelegramUser]] = db.run(telegramUsers.filter(_.chatId === chatId).result.headOption)

  def findByFollowerIds(ids: Seq[Long]): Future[Seq[TelegramUser]] = db.run(telegramUsers.filter(_.id inSet ids).result)

  private class TelegramUserTable(tag: Tag) extends Table[TelegramUser](tag, "telegram_user") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def chatId = column[Long]("chat_id")

    def username = column[String]("username")

    override def * = (id, chatId, username) <> (TelegramUser.tupled, TelegramUser.unapply)
  }

}