package com.maantrack.domain.user

import cats.data.OptionT

trait UserRepository[F[_]] {
  def addUser(userRequest: UserRequest): F[User]

  def updateUser(user: User): OptionT[F, User]

  def getUserById(userId: Long): OptionT[F, User]

  def deleteUserById(userId: Long): OptionT[F, User]

  def getUserByUserName(userName: String): OptionT[F, User]
}
