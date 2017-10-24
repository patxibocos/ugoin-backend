package models.db

import javax.inject.Inject

import models.entities.Device
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

class DeviceDbRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private lazy val devices = TableQuery[DeviceTable]

  def insert(device: Device): Future[Long] = db.run(devices returning devices.map(_.id) += device)

  private class DeviceTable(tag: Tag) extends Table[Device](tag, "device") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def push = column[String]("push")

    def userId = column[Long]("user")

    def * = (id, name, push, userId) <> (Device.tupled, Device.unapply)
  }

}