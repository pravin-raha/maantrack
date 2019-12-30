package com.maantrack.domain.user

import cats.data.OptionT
import cats.effect.Async

class UserService[F[_]: Async](userRepository: UserRepository[F]) {
  def addUser(userRequest: UserRequest): F[User] =
    userRepository.addUser(userRequest)

  def getUserById(id: Long): OptionT[F, User] = userRepository.getUserById(id)

  def updateUser(user: User): OptionT[F, User] = userRepository.updateUser(user)

  def deleteUserById(id: Long): OptionT[F, User] =
    userRepository.deleteUserById(id)

  def getUserByUserName(userName: String): OptionT[F, User] =
    userRepository.getUserByUserName(userName)
}

object UserService {
  def apply[F[_]: Async](userRepository: UserRepository[F]): UserService[F] =
    new UserService(userRepository)
}
