package services

import javax.inject.{Inject, Singleton}

import models.db.UserDbRepository
import models.entities.User
import play.api.libs.Codecs

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UserService @Inject()(userDbRepository: UserDbRepository) {

  def login(username: String, password: String): Future[Option[String]] = {
    userDbRepository.findByName(username).map {
      case Some(u) => if (userPasswordMatches(password, u)) Some(u.token) else None
      case None => None
    }
  }

  def hashPassword(password: String, salt: String): String = Codecs.sha1(password + salt)

  private def userPasswordMatches(password: String, user: User): Boolean = user.password.contains(hashPassword(password, user.salt))

}
