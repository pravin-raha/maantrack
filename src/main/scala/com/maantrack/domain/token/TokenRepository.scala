package com.maantrack.domain.token

import cats.data.OptionT
import com.maantrack.auth.BearerToken
import tsec.common.SecureRandomId

trait TokenRepository[F[_]] {

  def addToken(bearerToken: BearerToken): F[BearerToken]

  def updateToken(bearerToken: BearerToken): F[BearerToken]

  def deleteTokenById(secureRandomId: SecureRandomId): F[Unit]

  def getTokenById(secureRandomId: SecureRandomId): OptionT[F, BearerToken]

}
