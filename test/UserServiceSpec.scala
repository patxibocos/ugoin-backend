import base.BaseSpec
import models.db.UserDbRepository
import models.entities.User
import play.api.Application
import services.UserService

class UserServiceSpec extends BaseSpec {

  def userDao(implicit app: Application) = Application.instanceCache[UserDbRepository].apply(app)

  val userService = new UserService(userDao)

  "User service" should {

    "login is successful only if credentials are valid" in {
      val username = "user"
      val password = "pass"
      val salt = "salt"
      val token = "token"
      val hashedPassword = userService.hashPassword(password, salt)
      val user = User(name = username, email = None, password = hashedPassword, salt = salt, token = token)
      whenReady(userDao.insert(user)) { _ =>
        whenReady(userService.login(username, password)) { result =>
          result mustBe Some(token)
        }
        whenReady(userService.login(username, "incorrectPassword")) { result =>
          result mustBe None
        }
      }
    }

  }

}
