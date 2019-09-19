package com.maantrack.endpoint

import cats.implicits._
import cats.effect.Sync
import com.maantrack.domain.cardlist.{ CardListRequest, CardListService }
import com.maantrack.domain.user.User
import org.http4s.dsl.Http4sDsl
import tsec.authentication.{ SecuredRequestHandler, TSecAuthService, TSecBearerToken, asAuthed }
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.circe._

class CardListServiceEndpoint[F[_]: Sync](
  cardListService: CardListService[F],
  Auth: SecuredRequestHandler[F, Long, User, TSecBearerToken[Long]]
) extends Http4sDsl[F] {

  private val authService: AuthService[F] = TSecAuthService {
    case req @ POST -> Root asAuthed _ =>
      for {
        cardListReq <- req.request.as[CardListRequest]
        id          <- cardListService.add(cardListReq)
        res         <- Ok(id.asJson)
      } yield res

    case GET -> Root / LongVar(cardListId) asAuthed _ =>
      cardListService.getById(cardListId).value.flatMap {
        case Some(list) => Ok(list.asJson)
        case None       => NotFound(s"List with list id $cardListId not found".asJson)
      }

    case DELETE -> Root / LongVar(cardListId) asAuthed _ =>
      cardListService.deleteById(cardListId).value.flatMap {
        case Some(list) => Ok(list.asJson)
        case None       => NotFound(s"List with list id $cardListId not found".asJson)
      }
  }

  val service: HttpRoutes[F] = Auth.liftService(authService)
}
