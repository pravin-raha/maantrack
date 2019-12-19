package com.maantrack.endpoint

import cats.effect.Sync
import cats.implicits._
import com.maantrack.domain.cardlist.{ CardListRequest, CardListService }
import com.maantrack.domain.user.User
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.AuthedRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class CardListServiceEndpoint[F[_]: Sync](
  cardListService: CardListService[F]
) extends Http4sDsl[F] {

  private val authService: AuthedRoutes[User, F] = AuthedRoutes.of {
    case req @ POST -> Root as _ =>
      for {
        cardListReq <- req.req.as[CardListRequest]
        id          <- cardListService.add(cardListReq)
        res         <- Ok(id.asJson)
      } yield res

    case GET -> Root / LongVar(cardListId) as _ =>
      cardListService.getById(cardListId).value.flatMap {
        case Some(list) => Ok(list.asJson)
        case None       => NotFound(s"List with list id $cardListId not found".asJson)
      }

    case DELETE -> Root / LongVar(cardListId) as _ =>
      cardListService.deleteById(cardListId).flatMap(list => Ok(list.asJson))
  }

  val privateService: AuthedRoutes[User, F] = authService
}
