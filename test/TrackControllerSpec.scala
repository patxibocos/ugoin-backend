import base.BaseSpec
import controllers.TrackController
import models.db.UserDbRepository
import models.entities.User
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.Application
import play.api.mvc.Results._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrackingService

import scala.concurrent.Future

class TrackControllerSpec extends BaseSpec with MockitoSugar {

  private def userDao(implicit app: Application) = Application.instanceCache[UserDbRepository].apply(app)

  private val mockedTrackingService = mock[TrackingService]

  private val controller = new TrackController(userDao, mockedTrackingService, stubControllerComponents())

  "unauthorized request is rejected" in {
    val result = controller.start().apply(FakeRequest())
    whenReady(result) { r =>
      r mustBe Unauthorized
    }
  }

  "authorized request is processed" in {
    val token = "t0k3n"
    whenReady(userDao.insert(User(token = token, name = "name", email = None, password = "", salt = ""))) { _ =>
      val request = FakeRequest().withHeaders(("token", token))
      when(mockedTrackingService.createNewTrack(any[User])).thenReturn(Future.successful(1L))
      val result = controller.start().apply(request)
      whenReady(result) { r =>
        r mustBe Ok("")
      }
    }
  }

}
