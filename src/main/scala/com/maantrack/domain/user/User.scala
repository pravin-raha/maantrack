package com.maantrack.domain.user

import java.time.Instant

import cats.effect.IO
import cats.implicits._
import cats.{ Eq, MonadError }
import tsec.authentication.AugmentedJWT
import tsec.authorization.{ AuthGroup, AuthorizationInfo, BasicRBAC, SimpleAuthEnum }
import tsec.mac.jca.HMACSHA256

sealed case class Role(roleRepr: String)

case class User(
  userId: Long,
  avatarUrl: Option[String] = None,
  avatarSource: Option[String] = None,
  bio: Option[String] = None,
  confirmed: Option[Boolean] = Some(false),
  email: String,
  firsName: String,
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
  firsName: String,
  lastName: String,
  userType: Role = Role.Customer,
  password: String,
  userName: String,
  birthDate: Instant
)

case class UserResponse(
  userId: Long,
  avatarUrl: Option[String] = None,
  avatarSource: Option[String] = None,
  bio: Option[String] = None,
  confirmed: Option[Boolean] = Some(false),
  email: String,
  firsName: String,
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
  val AdminRequired: BasicRBAC[IO, Role, User, AugmentedJWT[HMACSHA256, Long]] =
    BasicRBAC[IO, Role, User, AugmentedJWT[HMACSHA256, Long]](Administrator)
  val CustomerRequired: BasicRBAC[IO, Role, User, AugmentedJWT[HMACSHA256, Long]] =
    BasicRBAC[IO, Role, User, AugmentedJWT[HMACSHA256, Long]](
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
