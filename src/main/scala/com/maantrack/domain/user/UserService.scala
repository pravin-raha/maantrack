package com.maantrack.domain.user

import cats.data.OptionT
import cats.effect.Async

class UserService[F[_]: Async](userRepository: UserRepository[F]) {

  def addUser(userRequest: UserRequest): F[User] =
    userRepository.addUser(userRequest)

  def getUserById(id: Int): OptionT[F, User] = userRepository.getUserById(id)

  def updateUser(user: User): F[User] = userRepository.updateUser(user)

  def deleteUserById(id: Int): F[User] = userRepository.deleteUserById(id)

  def getUserByUserName(userName: String): OptionT[F, User] =
    userRepository.getUserByUserName(userName)
}
