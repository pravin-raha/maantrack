package com.maantrack.repository.doobies.interpreter

import cats.data.OptionT
import cats.effect.Sync
import cats.syntax.all._
import com.maantrack.db.{ Decoders, Encoders, Schema }
import com.maantrack.domain.{ User, UserRequest, UsernameAlreadyExist }
import com.maantrack.repository.doobies.UserRepository
import doobie.implicits._
import doobie.postgres.sqlstate
import doobie.quill.DoobieContext.Postgres
import doobie.util.transactor.Transactor
import org.typelevel.log4cats.Logger
import io.getquill.{ EntityQuery, SnakeCase }

class UserRepositoryInterpreter[F[_]: Sync: Logger](
  xa: Transactor[F],
  override val ctx: Postgres[SnakeCase] with Decoders with Encoders
) extends UserRepository[F]
    with Schema {
  import ctx._
  implicit val userUpdateMeta: UpdateMeta[User] = updateMeta[User](_.userId)

  private def selectUserById(id: Long): Quoted[EntityQuery[User]] = quote {
    userSchema.filter(_.userId == lift(id))
  }

  private def selectUserByUserName(userName: String): Quoted[EntityQuery[User]] = quote {
    userSchema.filter(_.userName == lift(userName))
  }

  override def addUser(userRequest: UserRequest): F[User] =
    run(quote {
      userSchema.insert(lift(userRequest.toUser)).returningGenerated(_.userId)
    }).transact(xa)
      .map(userId => userRequest.toUser.copy(userId = userId))
      .attemptSomeSqlState {
        case sqlstate.class23.UNIQUE_VIOLATION => UsernameAlreadyExist(userRequest.userName)
      }
      .flatMap {
        case Left(e)     => e.raiseError[F, User]
        case Right(user) => user.pure[F]
      }

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
