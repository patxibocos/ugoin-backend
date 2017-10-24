package controllers

import javax.inject.{Inject, Singleton}

import dto.in.LoginUserRequestDTO.loginUserRequestReads
import dto.in.RegisterUserRequestDTO.registerUserRequestReads
import dto.in.{LoginUserRequest, RegisterUserRequest}
import dto.out.LoginResponseDTO.{LoginResponse, loginResponseWrites}
import io.swagger.annotations._
import models.db.UserDbRepository
import models.entities.User
import play.api.libs.Codecs
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc._
import services.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Api(value = "user")
@Singleton
class UserController @Inject()(userDbRepository: UserDbRepository, userService: UserService, cc: ControllerComponents) extends AbstractController(cc) {

  def login = Action.async(parse.json) { implicit request: Request[JsValue] =>
    request.body.validate[LoginUserRequest] match {
      case s: JsSuccess[LoginUserRequest] =>
        userService.login(s.get.username, s.get.password).map {
          case Some(token) => Ok(Json.toJson(LoginResponse(token)))
          case None => Unauthorized
        }
      case e: JsError => Future.successful(BadRequest(JsError.toJson(e)))
    }
  }

  @ApiOperation(value = "Registers a user", consumes = "application/json")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "User data", value = "User data", paramType = "body", dataType = "dto.in.RegisterUserRequest")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "Success", response = classOf[LoginResponse]),
    new ApiResponse(code = 400, message = "Missing data"),
    new ApiResponse(code = 409, message = "Username already exists")
  ))
  def register = Action.async(parse.json) { implicit request: Request[JsValue] =>
    request.body.validate[RegisterUserRequest] match {
      case s: JsSuccess[RegisterUserRequest] =>
        userDbRepository.findByName(s.get.username).flatMap {
          case Some(_) => Future.successful(Conflict("Username already exists"))
          case None =>
            val salt = Codecs.sha1(System.currentTimeMillis().toString)
            val password = userService.hashPassword(s.get.password, salt)
            val user = User(name = s.get.username, email = None, password = password, salt = salt)
            userDbRepository.insert(user).map(_ => Ok(Json.toJson(LoginResponse(user.token))))
        }
      case e: JsError => Future.successful(BadRequest(JsError.toJson(e)))
    }
  }

}
