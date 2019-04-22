package com.maantrack.domain.user

import cats.data.OptionT

trait UserRepository[F[_]] {

  def addUser(userRequest: UserRequest): F[User]

  def updateUser(user: User): F[User]

  def getUserById(userId: Int): OptionT[F, User]

  def deleteUserById(user: Int): F[User]

  def getUserByUserName(userName: String): OptionT[F, User]

}
