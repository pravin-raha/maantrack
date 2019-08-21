package com.maantrack.repository.doobies

import cats.Monad
import cats.data.OptionT
import cats.effect.Async
import cats.implicits._
import com.maantrack.auth.BearerToken
import com.maantrack.domain.token.TokenRepository
import doobie.hikari.HikariTransactor
import doobie.util.fragment.Fragment
import doobie.util.log.LogHandler
import doobie.{ Query0, Update0 }
import tsec.common.SecureRandomId
import doobie._
import doobie.implicits._

object BearerSQL {
  import Fragments.whereAnd

  def byUserId(userId: Long): doobie.Query0[BearerToken] =
    (select ++ whereAnd(fr"  where user_id = $userId"))
      .queryWithLogHandler[BearerToken](LogHandler.jdkLogHandler)

  def byId(secureId: SecureRandomId): Query0[BearerToken] =
    (select ++ whereAnd(fr"where secure_id = $secureId"))
      .queryWithLogHandler[BearerToken](LogHandler.jdkLogHandler)

  def select: Fragment = fr"select secure_id, user_id, expiry, last_touched from token"

  def byUsername(userId: String): Query0[BearerToken] =
    (select ++ whereAnd(fr"where user_id = $userId"))
      .queryWithLogHandler[BearerToken](LogHandler.jdkLogHandler)

  def insert(u: BearerToken): Update0 = sql"""
    insert into token (secure_id, user_id, expiry, last_touched)
    values (${u.id}, ${u.identity}, ${u.expiry}, ${u.lastTouched})
  """.update

  def update(id: SecureRandomId, token: BearerToken): Update0 = sql"""
    update token
    set last_touched = ${token.expiry}
    where secure_id = $id
  """.update

  def delete(id: SecureRandomId): Update0 = sql"""
    delete from token
    where secure_id = $id
  """.update
}

class TokenRepositoryInterpreter[F[_]: Async](xa: HikariTransactor[F]) extends TokenRepository[F] {

  import BearerSQL._

  override def addToken(bearerToken: BearerToken): F[BearerToken] =
    insert(bearerToken).run
      .transact(xa)
      .as(bearerToken)

  override def updateToken(bearerToken: BearerToken): F[BearerToken] =
    update(bearerToken.id, bearerToken).run.transact(xa).as(bearerToken)

  override def deleteTokenById(secureRandomId: SecureRandomId): F[Unit] =
    delete(secureRandomId).run.transact(xa).map(_ => ())

  override def getTokenById(
    secureRandomId: SecureRandomId
  ): OptionT[F, BearerToken] = OptionT(byId(secureRandomId).option.transact(xa))

}

object TokenRepositoryInterpreter {
  def apply[F[_]: Monad: Async](
    xa: HikariTransactor[F]
  ): TokenRepositoryInterpreter[F] =
    new TokenRepositoryInterpreter[F](xa)
}
