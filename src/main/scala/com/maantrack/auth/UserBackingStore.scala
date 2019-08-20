package com.maantrack.auth

import cats.data.OptionT
import cats.effect.Async
import cats.implicits._
import com.maantrack.domain.Error
import com.maantrack.domain.user.{ User, UserRequest, UserService }
import io.scalaland.chimney.dsl._
import tsec.authentication.BackingStore

trait UserBackingStore[F[_]] extends BackingStore[F, Long, User]

object UserBackingStore {
  def apply[M[_]: Async](
    userService: UserService[M]
  ): UserBackingStore[M] =
    new UserBackingStore[M] {
      def put(user: User): M[User] =
        userService.addUser(user.into[UserRequest].transform)
      def get(id: Long): OptionT[M, User] = userService.getUserById(id)
      def update(user: User): M[User] =
        userService
          .updateUser(user)
          .toRight(Error.NotFound(): Throwable)
          .value
          .flatMap(_.raiseOrPure[M])
      def delete(id: Long): M[Unit] =
        userService
          .deleteUserById(id)
          .toRight(Error.NotFound(): Throwable)
          .value
          .flatMap(_.raiseOrPure[M])
          .map(_ => ())
    }
}
