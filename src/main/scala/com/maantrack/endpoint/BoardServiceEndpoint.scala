package com.maantrack.endpoint

import cats.effect._
import cats.implicits._
import com.maantrack.domain.{ BoardRequest, User }
import com.maantrack.service.BoardService
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.{ AuthMiddleware, Router }
import org.http4s.{ AuthedRoutes, HttpRoutes }

class BoardServiceEndpoint[F[_]: Sync](
  boardService: BoardService[F]
) extends Http4sDsl[F] {

  private val httpRoutes: AuthedRoutes[User, F] = AuthedRoutes.of {
    case req @ POST -> Root as user =>
      for {
        board  <- req.req.as[BoardRequest]
        id     <- boardService.add(user.userId, board)
        result <- Ok(id.asJson)
      } yield result

    case GET -> Root / LongVar(boardId) as _ =>
      boardService.getById(boardId).value.flatMap {
        case Some(board) => Ok(board.asJson)
        case None        => NotFound(s"Board with board id $boardId not found".asJson)
      }

    case DELETE -> Root / LongVar(boardId) as _ =>
      boardService.deleteById(boardId).flatMap { boardId =>
        Ok(boardId.asJson)
      }
  }

  private val prefixPath = "/board"

  def routes(authMiddleware: AuthMiddleware[F, User]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}

object BoardServiceEndpoint {
  def apply[F[_]: Sync](
    boardService: BoardService[F]
  )(implicit F: ConcurrentEffect[F]): BoardServiceEndpoint[F] = new BoardServiceEndpoint(boardService)
}
