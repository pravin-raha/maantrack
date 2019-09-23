package com.maantrack.repository.doobies

import java.time.Instant

import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import com.maantrack.domain.user.{ User, UserRepository, UserRequest }
import com.maantrack.repository.doobies.Doobie._
import doobie.Fragments
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.update.Update0
import io.chrisdavenport.log4cats.Logger
import io.scalaland.chimney.dsl._

private object UserSql {
  import Fragments.whereAnd

  private val tableName: Fragment = Fragment.const("app_user")

  def insert[F[_]: Logger](userRequest: UserRequest): Update0 =
    (fr"""INSERT INTO""" ++ tableName ++
      fr"""( email, first_name, last_name, user_type, password, birth_date,
             user_name, created_date, modified_date )
          VALUES
          (
             ${userRequest.email}, ${userRequest.firsName}, ${userRequest.lastName}, ${userRequest.userType}
           , ${userRequest.password}, ${userRequest.birthDate}
           , ${userRequest.userName}, NOW(), NOW()
          )""").update

  def selectById[F[_]: Logger](id: Long): doobie.Query0[User] =
    (select ++ whereAnd(fr"app_user_id = $id")).query[User]

  def delete[F[_]: Logger](id: Long): doobie.Update0 =
    (fr"DELETE FROM" ++ tableName ++ fr"app_user WHERE app_user_id = $id").update

  def update[F[_]: Logger](user: User): doobie.Update0 =
    (fr"UPDATE" ++ tableName ++ fr"set app_user_id = ${user.userId}, name = ${user.firsName}, email = ${user.email} WHERE app_user_id = ${user.userId}").update

  def selectByUserName[F[_]: Logger](username: String): doobie.Query0[User] =
    (select ++ whereAnd(fr"user_name = $username"))
      .query[User]

  private val select =
    fr"""select app_user_id, avatar_url, avatar_source, bio, confirmed , email, first_name, last_name,
                user_type, profile_url, password, user_name, birth_date, created_date, modified_date 
         from""" ++ tableName
}

class UserRepositoryInterpreter[F[_]: Sync: Logger](xa: HikariTransactor[F]) extends UserRepository[F] {

  import UserSql._

  private val now = Instant.now()

  override def addUser(userRequest: UserRequest): F[User] =
    insert(userRequest)
      .withUniqueGeneratedKeys[Long]("app_user_id")
      .map(
        id =>
          userRequest
            .into[User]
            .withFieldConst(_.userId, id)
            .withFieldConst(_.modifiedDate, now)
            .withFieldConst(_.createdDate, now)
            .transform
      )
      .transact(xa)

  override def deleteUserById(id: Long): OptionT[F, User] =
    getUserById(id)
      .semiflatMap(user => delete(id).run.transact(xa).as(user))

  override def getUserById(id: Long): OptionT[F, User] =
    OptionT(selectById(id).option.transact(xa))

  override def updateUser(user: User): OptionT[F, User] =
    OptionT.liftF(UserSql.update(user).run.transact(xa).as(user))

  override def getUserByUserName(userName: String): OptionT[F, User] =
    OptionT(selectByUserName(userName).option.transact(xa))
}

object UserRepositoryInterpreter {
  def apply[F[_]: Sync: Logger](
    xa: HikariTransactor[F]
  ): UserRepositoryInterpreter[F] =
    new UserRepositoryInterpreter[F](xa)
}
