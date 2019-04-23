package com.maantrack.domain.user

import cats.effect.IO
import cats.implicits._
import cats.{Eq, MonadError}
import tsec.authentication.AugmentedJWT
import tsec.authorization.{
  AuthGroup,
  AuthorizationInfo,
  BasicRBAC,
  SimpleAuthEnum
}
import tsec.mac.jca.HMACSHA256

sealed case class Role(roleRepr: String)

case class User(
    id: Long,
    age: Int,
    name: String,
    userName: String,
    role: Role = Role.Customer,
    password: String,
    email: String
)

case class UserRequest(
    age: Int,
    name: String,
    userName: String,
    role: Role = Role.Customer,
    password: String,
    email: String
)

case class UserResponse(
    id: Long,
    age: Int,
    name: String,
    role: Role,
    email: String,
    userName: String
)

case class UserCredential(userName: String, password: String)

object Role extends SimpleAuthEnum[Role, String] {
  lazy val Customer: Role = Role("User")
  lazy val Administrator: Role = Role("Administrator")
  lazy val Seller: Role = Role("Seller")

  implicit val E: Eq[Role] = Eq.fromUniversalEquals[Role]
  val AdminRequired: BasicRBAC[IO, Role, User, AugmentedJWT[HMACSHA256, Long]] =
    BasicRBAC[IO, Role, User, AugmentedJWT[HMACSHA256, Long]](Administrator)
  val CustomerRequired
      : BasicRBAC[IO, Role, User, AugmentedJWT[HMACSHA256, Long]] =
    BasicRBAC[IO, Role, User, AugmentedJWT[HMACSHA256, Long]](
      Administrator,
      Customer
    )
  protected val values: AuthGroup[Role] =
    AuthGroup(Administrator, Customer, Seller)

  override def getRepr(t: Role): String = t.roleRepr
}

object User {
  implicit def authRole[F[_]](
      implicit F: MonadError[F, Throwable]
  ): AuthorizationInfo[F, Role, User] =
    (u: User) => F.pure(u.role)
}
