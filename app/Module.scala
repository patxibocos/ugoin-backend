import java.time.Clock

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import tasks.{TelegramActor, TelegramUpdatesActor, TwitterActor, TwitterDmsActor}

/**
  * This class is a Guice module that tells Guice how to bind several
  * different types. This Guice module is created when the Play
  * application starts.
  *
  * Play will automatically use any class called `Module` that is in
  * the root package. You can create modules in other locations by
  * adding `play.modules.enabled` settings to the `application.conf`
  * configuration file.
  */
class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure() = {
    // Use the system clock as the default implementation of Clock
    bindActor[TwitterDmsActor]("twitter-dms-actor")
    bindActor[TelegramUpdatesActor]("telegram-updates-actor")
    bindActor[TelegramActor]("telegram-actor")
    bindActor[TwitterActor]("twitter-actor")
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
  }

}
