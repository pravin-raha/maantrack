package com.maantrack

import cats.effect.Sync
import com.maantrack.domain.user.{UserCredential, UserRequest}
import io.circe.generic.auto._
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

package object endpoint {

  implicit def userRequestDecoder[F[_]: Sync]: EntityDecoder[F, UserRequest] =
    jsonOf[F, UserRequest]

  implicit def userCreadential[F[_]: Sync]: EntityDecoder[F, UserCredential] =
    jsonOf[F, UserCredential]
}
