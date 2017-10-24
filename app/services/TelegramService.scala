package services

import javax.inject.{Inject, Singleton}

import dto.in.TelegramRequestDTO.UserRequest
import models.SocialCommand
import models.db.{SourceDbRepository, TelegramUserDbRepository}
import models.entities.TelegramUser
import play.api.Configuration
import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class TelegramService @Inject()(socialService: SocialService, sourceDbRepository: SourceDbRepository, telegramUserDbRepository: TelegramUserDbRepository, ws: WSClient, configuration: Configuration, messagesApi: MessagesApi) {

  def sendLocation(followerIds: Seq[Long], name: String, latitude: Double, longitude: Double)(implicit lang: Lang) = {
    for {
      telegramUsers <- telegramUserDbRepository.findByFollowerIds(followerIds)
      result <- sendLocationToFollowers(name, latitude, longitude, telegramUsers)
    } yield result
  }


  private val botId = configuration.get[String]("telegram.bot.id")
  private val botToken = configuration.get[String]("telegram.bot.token")
  private val baseUrl = s"https://api.telegram.org/$botId:$botToken"

  def sendLocationToFollowers(username: String, latitude: Double, longitude: Double, followers: Seq[TelegramUser])(implicit lang: Lang): Future[Seq[WSResponse]] = {
    Future.sequence(followers.map(telegramUser => sendMessage(telegramUser.chatId, messagesApi("user_location")).flatMap(_ =>
      ws.url(s"$baseUrl/sendLocation?latitude=$latitude&longitude=$longitude&chat_id=${telegramUser.chatId}").get()
    )))
  }

  def sendMessage(chatId: Long, message: String): Future[WSResponse] = {
    ws.url(s"$baseUrl/sendMessage?text=$message&chat_id=$chatId").get()
  }

  def getUpdates(sinceId: Long) = ws.url(s"$baseUrl/getUpdates?offset=${sinceId + 1}").get()

  def identifyUser(username: String, chatId: Long): Future[Long] = {
    telegramUserDbRepository.findByChatId(chatId).flatMap {
      case Some(follower) => Future.successful(follower.id)
      case None => telegramUserDbRepository.insert(TelegramUser(chatId = chatId, username = username))
    }
  }

  def executeCommand(socialCommand: SocialCommand, chat: UserRequest, from: UserRequest)(implicit lang: Lang): Future[Boolean] = {
    (for {
      followerId <- identifyUser(from.username, chat.id)
      message <- socialService.executeCommand(socialCommand, followerId, sourceDbRepository.Telegram)
      response <- sendMessage(chat.id, message)
    } yield response).map(_.status == 200)
  }

}
