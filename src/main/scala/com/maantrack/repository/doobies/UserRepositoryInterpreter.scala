package com.maantrack.repository.doobies

import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import com.maantrack.db.{ Decoders, Encoders, Schema }
import com.maantrack.domain.user.{ User, UserRepository, UserRequest }
import doobie.implicits._
import doobie.quill.DoobieContext.Postgres
import doobie.util.transactor.Transactor
import io.chrisdavenport.log4cats.Logger
import io.getquill.SnakeCase

class UserRepositoryInterpreter[F[_]: Sync: Logger](
  xa: Transactor[F],
  override val ctx: Postgres[SnakeCase] with Decoders with Encoders
) extends UserRepository[F]
    with Schema {
  import ctx._
  implicit val userUpdateMeta: UpdateMeta[User] = updateMeta[User](_.userId)
//  implicit val userInsertMeta: InsertMeta[User] = insertMeta[User](_.userId)

  private def selectUserById(id: Long): Quoted[EntityQuery[User]] = quote {
    userSchema.filter(_.userId == lift(id))
  }

  private def selectUserByUserName(userName: String): Quoted[EntityQuery[User]] = quote {
    userSchema.filter(_.userName == lift(userName))
  }

  override def addUser(userRequest: UserRequest): F[User] =
    run(quote {
      userSchema.insert(lift(userRequest.toUser)).returningGenerated(_.userId)
    }).transact(xa).map(userId => userRequest.toUser.copy(userId = userId))

  override def deleteUserById(id: Long): OptionT[F, User] =
    getUserById(id)
      .semiflatMap(user => run(selectUserById(id).delete).transact(xa).as(user))

  override def getUserById(id: Long): OptionT[F, User] =
    OptionT(run(selectUserById(id)).transact(xa).map(_.headOption))

  override def updateUser(user: User): OptionT[F, User] =
    OptionT.liftF(run(quote {
      userSchema.filter(_.userId == user.userId).update(lift(user))
    }).transact(xa).as(user))

  override def getUserByUserName(userName: String): OptionT[F, User] =
    OptionT(run(selectUserByUserName(userName)).transact(xa).map(_.headOption))
}

object UserRepositoryInterpreter {
  def apply[F[_]: Sync: Logger](
    xa: Transactor[F],
    ctx: Postgres[SnakeCase] with Decoders with Encoders
  ): UserRepositoryInterpreter[F] =
    new UserRepositoryInterpreter[F](xa, ctx)
}
