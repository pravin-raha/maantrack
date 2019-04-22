package com.maantrack.domain.token

import cats.data.OptionT
import com.maantrack.auth.BearerToken
import tsec.common.SecureRandomId

class TokenService[F[_]](tokenRepository: TokenRepository[F]) {

  def addToken(bearerToken: BearerToken): F[BearerToken] =
    tokenRepository.addToken(bearerToken)

  def updateToken(bearerToken: BearerToken): F[BearerToken] =
    tokenRepository.updateToken(bearerToken)

  def deleteTokenById(secureRandomId: SecureRandomId): F[Unit] =
    tokenRepository.deleteTokenById(secureRandomId)

  def getTokenById(secureRandomId: SecureRandomId): OptionT[F, BearerToken] =
    tokenRepository.getTokenById(secureRandomId)
}
