package com.maantrack

import cats.effect.Sync
import com.maantrack.domain.board.BoardRequest
import com.maantrack.domain.user.{ User, UserCredential, UserRequest, UserResponse }
import io.circe.generic.auto._
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf
import tsec.authentication.{ TSecAuthService, TSecBearerToken }

package object endpoint {

  implicit def userRequestDecoder[F[_]: Sync]: EntityDecoder[F, UserRequest] =
    jsonOf[F, UserRequest]

  implicit def userCredential[F[_]: Sync]: EntityDecoder[F, UserCredential] =
    jsonOf[F, UserCredential]

  implicit def userDecoder[F[_]: Sync]: EntityDecoder[F, UserResponse] = jsonOf

  implicit def boardDecoder[F[_]: Sync]: EntityDecoder[F, BoardRequest] = jsonOf

  type AuthService[F[_]] = TSecAuthService[User, TSecBearerToken[Long], F]

}
