package com.maantrack.domain

import java.time.Instant

import cats.effect.Sync
import io.circe.generic.auto._
import io.circe.generic.extras.semiauto._
import io.circe.{ Decoder, Encoder }
import io.getquill.Embedded
import io.scalaland.chimney.dsl._
import org.http4s.circe.{ jsonEncoderOf, jsonOf }
import org.http4s.{ EntityDecoder, EntityEncoder }

import scala.util.control.NoStackTrace

sealed case class Role(roleRepr: String) extends Embedded

case class User(
  userId: Long,
  avatarUrl: Option[String] = None,
  avatarSource: Option[String] = None,
  bio: Option[String] = None,
  confirmed: Option[Boolean] = Some(false),
  email: String,
  firstName: String,
  lastName: String,
  userType: Role = Role.Customer,
  profileUrl: Option[String] = None,
  password: String,
  userName: String,
  birthDate: Instant,
  createdDate: Instant,
  modifiedDate: Instant
)

case class UserRequest(
  email: String,
  firstName: String,
  lastName: String,
  userType: Role = Role.Customer,
  password: String,
  userName: String,
  birthDate: Instant
) {
  self =>
  def toUser: User =
    self
      .into[User]
      .withFieldConst(_.userId, 0L)
      .withFieldConst(_.modifiedDate, Instant.now())
      .withFieldConst(_.createdDate, Instant.now())
      .enableOptionDefaultsToNone
      .transform
}

case class UserResponse(
  userId: Long,
  avatarUrl: Option[String] = None,
  avatarSource: Option[String] = None,
  bio: Option[String] = None,
  confirmed: Option[Boolean] = Some(false),
  email: String,
  firstName: String,
  lastName: Option[String] = None,
  userType: Role = Role.Customer,
  profileUrl: Option[String] = None,
  userName: String,
  birthDate: Instant,
  createdDate: Instant,
  modifiedDate: Instant
)

case class UserCredential(userName: String, password: String)

case class InvalidUserOrPassword(userName: String) extends NoStackTrace

object InvalidUserOrPassword {
  implicit def encoder[F[_]: Sync]: EntityEncoder[F, InvalidUserOrPassword] = jsonEncoderOf
  implicit def decoder[F[_]: Sync]: EntityDecoder[F, InvalidUserOrPassword] = jsonOf
}

object Role {
  lazy val Customer: Role      = Role("User")
  lazy val Administrator: Role = Role("Administrator")

  implicit val encoder: Encoder[Role] = deriveUnwrappedEncoder
  implicit val decoder: Decoder[Role] = deriveUnwrappedDecoder
}

object UserResponse {
  implicit def userRequestEnc[F[_]: Sync]: EntityEncoder[F, UserResponse] = jsonEncoderOf
  implicit def userRequestDec[F[_]: Sync]: EntityDecoder[F, UserResponse] = jsonOf
}

object UserRequest {
  implicit def userRequestEnc[F[_]: Sync]: EntityEncoder[F, UserRequest] = jsonEncoderOf
  implicit def userRequestDec[F[_]: Sync]: EntityDecoder[F, UserRequest] = jsonOf
}

object UserCredential {
  implicit def userCredentialEnc[F[_]: Sync]: EntityEncoder[F, UserCredential] = jsonEncoderOf
  implicit def userCredentialDec[F[_]: Sync]: EntityDecoder[F, UserCredential] = jsonOf
}

object User {
  implicit def encoder[F[_]: Sync]: EntityEncoder[F, User] = jsonEncoderOf
  implicit def decoder[F[_]: Sync]: EntityDecoder[F, User] = jsonOf
}
