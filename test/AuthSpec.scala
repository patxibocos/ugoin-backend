import base.BaseSpec
import controllers.auth.Auth
import models.db.UserDbRepository
import models.entities.User
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.Application
import play.api.mvc.{Headers, Results}
import play.api.test.FakeHeaders

class AuthSpec extends BaseSpec with Results with MockitoSugar with ScalaFutures {

  def userDao(implicit app: Application) = Application.instanceCache[UserDbRepository].apply(app)

  "Auth.isSuccessfulAuth" should {
    "return None User" in {
      val controller = new Auth {
        override val userDbRepository: UserDbRepository = mock[UserDbRepository]
      }
      val headers = FakeHeaders()
      val result = controller.isSuccessfulAuth(headers)
      whenReady(result) { user =>
        user mustBe None
      }
    }

    "return Some User" in {
      val controller = new Auth {
        override val userDbRepository: UserDbRepository = userDao
      }
      val token = "t0k3n"
      whenReady(userDao.insert(User(token = token, name = "test", email = None, password = "", salt = ""))) { _ =>
        val result = controller.isSuccessfulAuth(new Headers(List(("token", token))))
        whenReady(result) { user =>
          user mustBe defined
        }
      }
    }
  }

}
