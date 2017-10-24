package tasks

import javax.inject.{Named, Singleton}

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.Inject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

@Singleton
class MessagesCheckScheduler @Inject()(val system: ActorSystem, @Named("twitter-dms-actor") val twitterDmsActor: ActorRef, @Named("telegram-updates-actor") val telegramMessagesActor: ActorRef) {

  system.scheduler.schedule(0.microseconds, 5.seconds, twitterDmsActor, "")
  system.scheduler.schedule(0.microseconds, 5.seconds, telegramMessagesActor, "")

}
