package com.maantrack.repository.doobies

import java.time.Instant

import cats._
import cats.data.OptionT
import cats.effect.Async
import cats.implicits._
import com.maantrack.domain.user.{ User, UserRepository, UserRequest }
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.log.LogHandler
import doobie.util.update.Update0
import io.scalaland.chimney.dsl._

private object UserSql {
  private val now = Instant.now()

  def insert(userRequest: UserRequest): Update0 =
    sql"""INSERT INTO user
         ( email, first_name, last_name, user_type, password, birth_date,
           user_name, created_date, modified_date )
         VALUES
           (
             ${userRequest.email}, ${userRequest.firsName}, ${userRequest.lastName}, ${userRequest.userType}
           , ${userRequest.password}, ${userRequest.birthDate}
           , ${userRequest.userName}, $now, $now
           )"""
      .updateWithLogHandler(LogHandler.jdkLogHandler)

  def select(id: Long): doobie.Query0[User] = sql"""
      SELECT user_id, age, name, username, role, password, email
      FROM user
      WHERE user_id = $id
    """.queryWithLogHandler[User](LogHandler.jdkLogHandler)

  def delete(id: Long): doobie.Update0 =
    sql"DELETE FROM users WHERE users_id = $id"
      .updateWithLogHandler(LogHandler.jdkLogHandler)

  def update(user: User): doobie.Update0 =
    sql"UPDATE user set user_id = ${user.usersId}, name = ${user.firsName}, email = ${user.email} WHERE user_id = ${user.usersId}"
      .updateWithLogHandler(LogHandler.jdkLogHandler)

  def selectByUserName(username: String): doobie.Query0[User] = sql"""
      SELECT user_id, age, name, username, role, password, email
      FROM user
      WHERE username = $username
    """.queryWithLogHandler[User](LogHandler.jdkLogHandler)
}

class UserRepositoryInterpreter[F[_]: Async](xa: HikariTransactor[F]) extends UserRepository[F] {

  import UserSql._

  private val now = Instant.now()

  override def addUser(userRequest: UserRequest): F[User] =
    insert(userRequest)
      .withUniqueGeneratedKeys[Long]("user_id")
      .map(
        id =>
          userRequest
            .into[User]
            .withFieldConst(_.usersId, id)
            .withFieldConst(_.modifiedDate, now)
            .withFieldConst(_.createdDate, now)
            .transform
      )
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
