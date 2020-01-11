package com.maantrack.repository.doobies

import cats.data.OptionT
import com.maantrack.domain.{ User, UserRequest }

trait UserRepository[F[_]] {
  def addUser(userRequest: UserRequest): F[User]

  def updateUser(user: User): OptionT[F, User]

  def getUserById(userId: Long): OptionT[F, User]

  def deleteUserById(userId: Long): OptionT[F, User]

  def getUserByUserName(username: String): OptionT[F, User]

  def findUserByUsernameAndPassword(username: String, password: String): OptionT[F, User]
}
