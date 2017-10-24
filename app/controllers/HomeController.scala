package controllers

import javax.inject._

import play.api.mvc._

import scala.concurrent.Future

@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def index = Action.async {
    Future.successful(Ok("Hi!"))
  }

  def health = Action.async {
    Future.successful(Ok("Healthy"))
  }

}
