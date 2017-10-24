package tasks

import com.google.inject.AbstractModule

class SchedulerModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[MessagesCheckScheduler]).asEagerSingleton()
  }

}
