package com.maantrack.auth

import cats.data.OptionT
import com.maantrack.domain.token.TokenService
import tsec.authentication.{BackingStore, TSecBearerToken}
import tsec.common.SecureRandomId

trait TokenBackingStore[F[_]]
    extends BackingStore[F, SecureRandomId, TSecBearerToken[Int]]

object TokenBackingStore {
  def apply[F[_]](tokenService: TokenService[F]): TokenBackingStore[F] =
    new TokenBackingStore[F]() {
      override def put(
          bearerToken: TSecBearerToken[Int]
      ): F[TSecBearerToken[Int]] = tokenService.addToken(bearerToken)

      override def update(
          bearerToken: TSecBearerToken[Int]
      ): F[TSecBearerToken[Int]] = tokenService.updateToken(bearerToken)

      override def delete(id: SecureRandomId): F[Unit] =
        tokenService.deleteTokenById(id)

      override def get(id: SecureRandomId): OptionT[F, TSecBearerToken[Int]] =
        tokenService.getTokenById(id)
    }
}
