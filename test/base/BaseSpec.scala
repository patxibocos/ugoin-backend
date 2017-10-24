package base

import akka.stream.Materializer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import tasks.SchedulerModule

abstract class BaseSpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures {

  override def fakeApplication() = new GuiceApplicationBuilder().configure(Map(
    "slick.dbs.default.db.url" -> "jdbc:h2:mem:play;DATABASE_TO_UPPER=false;MODE=MYSQL;INIT=RUNSCRIPT FROM 'test.sql'",
    "slick.dbs.default.db.driver" -> "org.h2.Driver",
    "slick.dbs.default.driver" -> "slick.driver.H2Driver$"
  )).disable[SchedulerModule].build()

  implicit val defaultPatience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  implicit val materializer: Materializer = fakeApplication().injector.instanceOf[Materializer]

}
