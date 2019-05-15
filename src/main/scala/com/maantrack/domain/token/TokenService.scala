package com.maantrack.domain.token

import cats.data.OptionT
import cats.effect.Async
import com.maantrack.auth.BearerToken
import tsec.common.SecureRandomId

class TokenService[F[_]: Async](tokenRepository: TokenRepository[F]) {

  def addToken(bearerToken: BearerToken): F[BearerToken] =
    tokenRepository.addToken(bearerToken)

  def updateToken(bearerToken: BearerToken): F[BearerToken] =
    tokenRepository.updateToken(bearerToken)

  def deleteTokenById(secureRandomId: SecureRandomId): F[Unit] =
    tokenRepository.deleteTokenById(secureRandomId)

  def getTokenById(secureRandomId: SecureRandomId): OptionT[F, BearerToken] =
    tokenRepository.getTokenById(secureRandomId)
}

object TokenService {
  def apply[F[_]: Async](tokenRepository: TokenRepository[F]): TokenService[F] =
    new TokenService(tokenRepository)
}
