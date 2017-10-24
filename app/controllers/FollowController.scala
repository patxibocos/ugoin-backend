package controllers

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.auth.Auth
import dto.out.FollowerResponseDTO.followerDtoWrites
import models.db.{FollowDbRepository, UserDbRepository}
import models.entities.User
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import services.FollowService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class FollowController @Inject()(followService: FollowService, followDbRepository: FollowDbRepository, val userDbRepository: UserDbRepository, cc: ControllerComponents) extends AbstractController(cc) with Auth {

  def getFollowers = Action.async { implicit request: Request[AnyContent] =>
    ActionSecured { user: User =>
      followService.getFollowers(user).map(followers => Ok(Json.toJson(followers)))
    }
  }

  def acceptFollower(followId: Long) = Action.async { implicit request: Request[AnyContent] =>
    ActionSecured { user: User =>
      followDbRepository.findByIdAndFollowing(followId, user) flatMap {
        case None => Future.successful(NotFound)
        case Some(follow) => followService.acceptFollower(follow).flatMap(_ => Future.successful(Ok("")))
      }
    }
  }

  def rejectFollower(followId: Long) = Action.async { implicit request: Request[AnyContent] =>
    ActionSecured { user: User =>
      followDbRepository.findByIdAndFollowing(followId, user) flatMap {
        case None => Future.successful(NotFound)
        case Some(follow) => followService.rejectFollower(follow).flatMap(_ => Future.successful(Ok("")))
      }
    }
  }

}