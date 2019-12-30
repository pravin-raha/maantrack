package com.maantrack.repository.doobies

import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import com.maantrack.auth.BearerToken
import com.maantrack.domain.token.TokenRepository
import com.maantrack.repository.doobies.Doobie._
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.{ Query0, Update0, _ }
import io.chrisdavenport.log4cats.Logger
import tsec.common.SecureRandomId

object BearerSQL {
  import Fragments.whereAnd

  def byUserId(userId: Long): doobie.Query0[BearerToken] =
    (select ++ whereAnd(fr"app_user_id = $userId"))
      .query[BearerToken]

  def byId(secureId: SecureRandomId): Query0[BearerToken] =
    (select ++ whereAnd(fr"secure_id = $secureId"))
      .query[BearerToken]

  def select: Fragment = fr"select secure_id, app_user_id, expiry, last_touched from token "

  def byUsername(userId: String): Query0[BearerToken] =
    (select ++ whereAnd(fr"app_user_id = $userId"))
      .query[BearerToken]

  def insert(u: BearerToken): Update0 = sql"""
    insert into token (secure_id, app_user_id, expiry, last_touched)
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

class TokenRepositoryInterpreter[F[_]: Sync: Logger](xa: Transactor[F]) extends TokenRepository[F] {
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
  def apply[F[_]: Sync: Logger](
    xa: Transactor[F]
  ): TokenRepositoryInterpreter[F] =
    new TokenRepositoryInterpreter[F](xa)
}
