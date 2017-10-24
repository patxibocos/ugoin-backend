import base.BaseSpec
import controllers.UserController
import dto.in.{LoginUserRequest, RegisterUserRequest}
import models.db.UserDbRepository
import models.entities.User
import org.scalatest.mockito.MockitoSugar
import play.api.Application
import play.api.libs.json._
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import services.UserService

class UserControllerSpec extends BaseSpec with MockitoSugar {

  private def userDao(implicit app: Application) = Application.instanceCache[UserDbRepository].apply(app)

  private def userService(implicit app: Application) = Application.instanceCache[UserService].apply(app)

  private val controller = new UserController(userDao, userService, stubControllerComponents())

  implicit val registerUserRequestWrites: OWrites[RegisterUserRequest] = Json.writes[RegisterUserRequest]
  implicit val loginUserRequestWrites: OWrites[LoginUserRequest] = Json.writes[LoginUserRequest]

  "user should have a username when registers" in {
    val username = "patxi"
    val jsonBody = Json.toJson(RegisterUserRequest(username = username, password = ""))
    val request = FakeRequest().withBody[JsValue](jsonBody)
    val result = controller.register().apply(request)
    whenReady(result) { r =>
      whenReady(r.body.consumeData) { data =>
        val token = Json.parse(data.utf8String)("token").as[String]
        val user = userDao.findByToken(token)
        whenReady(user) { u =>
          u mustBe defined
          u.get.name mustBe username
        }
      }
    }
  }

  "user can login" in {
    val username = "patxi2"
    val password = "pass"
    val jsonBody = Json.toJson(LoginUserRequest(username = username, password = password))
    val request = FakeRequest().withBody[JsValue](jsonBody)
    val salt = "tomasalt"
    val hashedPassword = userService.hashPassword(password, salt)
    whenReady(userDao.insert(User(name = username, email = None, password = hashedPassword, salt = salt))) { _ =>
      val result = controller.login().apply(request)
      whenReady(result) { r =>
        whenReady(r.body.consumeData) { data =>
          val token = Json.parse(data.utf8String)("token").as[String]
          val user = userDao.findByToken(token)
          whenReady(user) { u =>
            u mustBe defined
            u.get.token mustBe token
          }
        }
      }
    }
  }

}
