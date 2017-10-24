package models.entities

import java.util.UUID

case class User(id: Long = 0, token: String = UUID.randomUUID().toString, name: String, email: Option[String], password: String, salt: String)