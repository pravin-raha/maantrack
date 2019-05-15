package com.maantrack.repository.doobies

import cats._
import cats.data.OptionT
import cats.effect.Async
import cats.implicits._
import com.maantrack.domain.user.{User, UserRepository, UserRequest}
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.log.LogHandler
import doobie.util.update.Update0
import io.scalaland.chimney.dsl._

import scala.language.higherKinds

private object UserSql {
  def insert(userRequest: UserRequest): Update0 =
    sql"""INSERT INTO users (name, email, password, age, username, role)
          VALUES (${userRequest.name}, ${userRequest.email}, ${userRequest.password},
          ${userRequest.age},${userRequest.userName},${userRequest.role.roleRepr})"""
      .updateWithLogHandler(LogHandler.jdkLogHandler)

  def select(id: Long): doobie.Query0[User] = sql"""
      SELECT users_id, age, name, username, role, password, email
      FROM users
      WHERE users_id = $id
    """.queryWithLogHandler[User](LogHandler.jdkLogHandler)

  def delete(id: Long): doobie.Update0 =
    sql"DELETE FROM users WHERE users_id = $id"
      .updateWithLogHandler(LogHandler.jdkLogHandler)

  def update(user: User): doobie.Update0 =
    sql"UPDATE users set users_id = ${user.id}, name = ${user.name}, email = ${user.email} WHERE user_id = ${user.id}"
      .updateWithLogHandler(LogHandler.jdkLogHandler)

  def selectByUserName(username: String): doobie.Query0[User] = sql"""
      SELECT users_id, age, name, username, role, password, email
      FROM users
      WHERE username = $username
    """.queryWithLogHandler[User](LogHandler.jdkLogHandler)
}

class UserRepositoryInterpreter[F[_]: Async](xa: HikariTransactor[F])
    extends UserRepository[F] {

  import UserSql._

  override def addUser(userRequest: UserRequest): F[User] =
    insert(userRequest)
      .withUniqueGeneratedKeys[Long]("users_id")
      .map(id => userRequest.into[User].withFieldConst(_.id, id).transform)
      .transact(xa)

  override def deleteUserById(id: Long): OptionT[F, User] =
    getUserById(id)
      .semiflatMap(user => delete(id).run.transact(xa).as(user))

  override def getUserById(id: Long): OptionT[F, User] =
    OptionT(select(id).option.transact(xa))

  override def updateUser(user: User): OptionT[F, User] =
    OptionT.liftF(UserSql.update(user).run.transact(xa).as(user))

  override def getUserByUserName(userName: String): OptionT[F, User] =
    OptionT(selectByUserName(userName).option.transact(xa))
}

object UserRepositoryInterpreter {
  def apply[F[_]: Monad: Async](
      xa: HikariTransactor[F]
  ): UserRepositoryInterpreter[F] =
    new UserRepositoryInterpreter[F](xa)
}
