package com.maantrack.domain.user

import java.time.Instant

import cats.effect.Sync
import cats.implicits._
import cats.{ Eq, MonadError }
import io.circe.generic.auto._
import io.getquill.Embedded
import io.scalaland.chimney.dsl._
import org.http4s.circe.{ jsonEncoderOf, jsonOf }
import org.http4s.{ EntityDecoder, EntityEncoder }
import tsec.authentication.AugmentedJWT
import tsec.authorization.{ AuthGroup, AuthorizationInfo, BasicRBAC, SimpleAuthEnum }
import tsec.mac.jca.HMACSHA256

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

object Role extends SimpleAuthEnum[Role, String] {
  lazy val Customer: Role      = Role("User")
  lazy val Administrator: Role = Role("Administrator")

  implicit val E: Eq[Role] = Eq.fromUniversalEquals[Role]
  def AdminRequired[F[_]: Sync]: BasicRBAC[F, Role, User, AugmentedJWT[HMACSHA256, Long]] =
    BasicRBAC[F, Role, User, AugmentedJWT[HMACSHA256, Long]](Administrator)
  def CustomerRequired[F[_]: Sync]: BasicRBAC[F, Role, User, AugmentedJWT[HMACSHA256, Long]] =
    BasicRBAC[F, Role, User, AugmentedJWT[HMACSHA256, Long]](
      Administrator,
      Customer
    )
  protected val values: AuthGroup[Role] =
    AuthGroup(Administrator, Customer)

  override def getRepr(t: Role): String = t.roleRepr
}

object User {
  implicit def authRole[F[_]](
    implicit F: MonadError[F, Throwable]
  ): AuthorizationInfo[F, Role, User] =
    (u: User) => F.pure(u.userType)
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
