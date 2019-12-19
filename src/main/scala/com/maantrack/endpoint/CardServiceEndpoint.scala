package com.maantrack.endpoint

import cats.effect.Sync
import cats.implicits._
import com.maantrack.domain.card.{ CardRequest, CardService }
import com.maantrack.domain.user.User
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.AuthedRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class CardServiceEndpoint[F[_]: Sync](
  cardService: CardService[F]
) extends Http4sDsl[F] {

  private val cardEndpoint: AuthedRoutes[User, F] = AuthedRoutes.of {
    case req @ POST -> Root as _ =>
      for {
        cardReq <- req.req.as[CardRequest]
        cardId  <- cardService.add(cardReq)
        res     <- Ok(cardId.asJson)
      } yield res

    case GET -> Root / LongVar(cardId) as _ =>
      cardService.getById(cardId).value.flatMap {
        case Some(card) => Ok(card.asJson)
        case None       => NotFound(s"Card with card id $cardId not found".asJson)
      }

    case DELETE -> Root / LongVar(cardId) as _ =>
      cardService.deleteById(cardId).flatMap(cId => Ok(cId.asJson))
  }

  val authService: AuthedRoutes[User, F] = cardEndpoint
}
