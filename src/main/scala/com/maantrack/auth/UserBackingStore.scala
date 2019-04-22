package com.maantrack.auth

import cats.Monad
import cats.data.OptionT
import cats.implicits._
import com.maantrack.domain.user.{User, UserRequest, UserService}
import io.scalaland.chimney.dsl._
import tsec.authentication.BackingStore

trait UserBackingStore[F[_]] extends BackingStore[F, Int, User]

object UserBackingStore {
  def apply[M[_]: Monad](userService: UserService[M]): UserBackingStore[M] =
    new UserBackingStore[M] {
      def put(user: User): M[User] =
        userService.addUser(user.into[UserRequest].transform)
      def get(id: Int): OptionT[M, User] = userService.getUserById(id)
      def update(user: User): M[User] = userService.updateUser(user)
      def delete(id: Int): M[Unit] =
        userService.deleteUserById(id).map(_ -> Unit)
    }
}
