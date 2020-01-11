package com.maantrack.endpoint

import cats.effect.Sync
import cats.implicits._
import com.maantrack.domain.{ CardListRequest, User }
import com.maantrack.service.CardListService
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{ AuthMiddleware, Router }
import org.http4s.{ AuthedRoutes, HttpRoutes }

class CardListServiceEndpoint[F[_]: Sync](
  cardListService: CardListService[F]
) extends Http4sDsl[F] {

  private val httpRoutes: AuthedRoutes[User, F] = AuthedRoutes.of {
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

  private val prefixPath = "/list"

  def routes(authMiddleware: AuthMiddleware[F, User]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )
}
