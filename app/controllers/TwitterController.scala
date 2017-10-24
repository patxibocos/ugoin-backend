package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.Future

@Singleton
class TwitterController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def twitter = Action.async {
    Future.successful(Ok(""))
  }

}
