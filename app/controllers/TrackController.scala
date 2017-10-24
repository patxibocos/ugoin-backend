package controllers

import javax.inject.{Inject, Singleton}

import controllers.auth.Auth
import models.db.UserDbRepository
import models.entities.User
import play.api.i18n.Lang
import play.api.mvc._
import services.TrackingService

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TrackController @Inject()(val userDbRepository: UserDbRepository, trackingService: TrackingService, cc: ControllerComponents) extends AbstractController(cc) with Auth {

  def check(latitude: Double, longitude: Double) = Action.async { implicit request: Request[AnyContent] =>
    ActionSecured { user: User =>
      (for {
        _ <- trackingService.saveUserCheckpoint(user, latitude, longitude)
        r <- trackingService.propagateLocationToFollowers(user, latitude, longitude)(Lang.defaultLang)
      } yield r).map(result => if (result) Ok("All right") else Ok("Something went wrong :("))
    }
  }

  def start = Action.async { implicit request: Request[AnyContent] =>
    ActionSecured { user: User =>
      trackingService.createNewTrack(user).map(_ => Ok(""))
    }
  }

  def end = Action.async { implicit request: Request[AnyContent] =>
    ActionSecured { user: User =>
      trackingService.endCurrentTrack(user).map(_ => Ok(""))
    }
  }

}
