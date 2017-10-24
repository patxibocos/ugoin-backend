package services

import javax.inject.{Inject, Singleton}

import models.SocialCommand
import models.db.{SourceDbRepository, TwitterUserDbRepository}
import models.entities.TwitterUser
import play.api.Configuration
import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.oauth._
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class TwitterService @Inject()(socialService: SocialService, sourceDbRepository: SourceDbRepository, twitterUserDbRepository: TwitterUserDbRepository, ws: WSClient, configuration: Configuration, messagesApi: MessagesApi) {

  private val consumerKey = ConsumerKey(configuration.get[String]("twitter.app.key"), configuration.get[String]("twitter.app.secret"))
  private val requestToken = RequestToken(configuration.get[String]("twitter.user.token"), configuration.get[String]("twitter.user.secret"))
  private val baseUrl = "https://api.twitter.com/1.1"

  def sendLocation(followerIds: Seq[Long], name: String, latitude: Double, longitude: Double)(implicit lang: Lang) = {
    for {
      twitterUsers <- twitterUserDbRepository.findByFollowerIds(followerIds)
      result <- sendLocationToFollowers(name, latitude, longitude, twitterUsers)
    } yield result
  }

  def sendLocationToFollowers(username: String, latitude: Double, longitude: Double, followers: Seq[TwitterUser])(implicit lang: Lang): Future[Seq[WSResponse]] = {
    Future.sequence(followers.map(twitterUser => sendMessage(twitterUser.userId, messagesApi("user_location"))))
  }

  def sendMessage(userId: Long, message: String): Future[WSResponse] =
    ws.url(s"$baseUrl/direct_messages/new.json?text=$message&user_id=$userId").sign(OAuthCalculator(consumerKey, requestToken)).post("")

  def getDms(sinceId: Long) = ws.url(s"$baseUrl/direct_messages.json?since_id=$sinceId").sign(OAuthCalculator(consumerKey, requestToken)).get()

  def identifyUser(userScreenName: String, userId: Long): Future[Long] = {
    twitterUserDbRepository.findByUserId(userId).flatMap {
      case Some(follower) => Future.successful(follower.id)
      case None => twitterUserDbRepository.insert(TwitterUser(userId = userId, userScreenName = userScreenName))
    }
  }

  def executeCommand(socialCommand: SocialCommand, userScreenName: String, userId: Long)(implicit lang: Lang) = {
    (for {
      followerId <- identifyUser(userScreenName, userId)
      message <- socialService.executeCommand(socialCommand, followerId, sourceDbRepository.Twitter)
      response <- sendMessage(userId, message)
    } yield response).map(_.status == 200)
  }

}
