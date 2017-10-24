package controllers.auth

import models.db.UserDbRepository
import models.entities.User
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Auth {

  val userDbRepository: UserDbRepository

  def ActionSecured[A](action: (User) => Future[Result])(implicit request: Request[A]): Future[Result] = {
    isSuccessfulAuth(request.headers).flatMap {
      case None => Future.successful(Unauthorized)
      case Some(user) =>
        action(user)
    }
  }

  def isSuccessfulAuth[A](headers: Headers): Future[Option[User]] = {
    headers.get("token") match {
      case Some(token) => userDbRepository.findByToken(token).flatMap {
        case Some(user) => Future.successful(Some(user))
        case None => Future.successful(None)
      }
      case None => Future.successful(None)
    }
  }

}
