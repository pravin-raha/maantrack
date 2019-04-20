package com.maantrack.auth

import cats._
import cats.data.OptionT
import cats.effect.{IO, Sync}
import cats.implicits._
import com.maantrack.auth.ExampleAuthHelpers.Role.{Administrator, Customer}
import tsec.authentication._
import tsec.authorization._
import tsec.mac.jca.HMACSHA256

import scala.collection.mutable

object ExampleAuthHelpers {

  val AdminRequired: BasicRBAC[IO, Role, User, AugmentedJWT[HMACSHA256, Int]] =
    BasicRBAC[IO, Role, User, AugmentedJWT[HMACSHA256, Int]](Administrator)
  val CustomerRequired
      : BasicRBAC[IO, Role, User, AugmentedJWT[HMACSHA256, Int]] =
    BasicRBAC[IO, Role, User, AugmentedJWT[HMACSHA256, Int]](
      Administrator,
      Customer
    )

  /** dummy factory for backing storage */
  def dummyBackingStore[F[_], I, V](
      getId: V => I
  )(implicit F: Sync[F]): BackingStore[F, I, V] =
    new BackingStore[F, I, V] {
      private val storageMap = mutable.HashMap.empty[I, V]

      def put(elem: V): F[V] = {
        val map = storageMap.put(getId(elem), elem)
        if (map.isEmpty)
          F.pure(elem)
        else
          F.raiseError(new IllegalArgumentException)
      }

      def get(id: I): OptionT[F, V] =
        OptionT.fromOption[F](storageMap.get(id))

      def update(v: V): F[V] = {
        storageMap.update(getId(v), v)
        F.pure(v)
      }

      def delete(id: I): F[Unit] =
        storageMap.remove(id) match {
          case Some(_) => F.unit
          case None    => F.raiseError(new IllegalArgumentException)
        }
    }

  /*
  In our example, we will demonstrate how to use SimpleAuthEnum, as well as
  Role based authorization
   */
  sealed case class Role(roleRepr: String)

  case class User(id: Int, age: Int, name: String, role: Role = Role.Customer)

  case class UserRequest(id: Int, age: Int, name: String)

  object Role extends SimpleAuthEnum[Role, String] {
    lazy val Customer: Role = Role("User")
    lazy val Administrator: Role = Role("Administrator")
    lazy val Seller: Role = Role("Seller")

    implicit val E: Eq[Role] = Eq.fromUniversalEquals[Role]
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

}
