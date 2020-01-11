package com.maantrack.endpoint

import cats.effect._
import cats.implicits._
import com.maantrack.domain.{ BoardRequest, User }
import com.maantrack.service.BoardService
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.{ AuthedRoutes, Response }
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class BoardServiceEndpoint[F[_]: Sync](
  boardService: BoardService[F]
) extends Http4sDsl[F] {

  private val boardCreateService: AuthedRoutes[User, F] = AuthedRoutes.of {
    case req @ POST -> Root as user =>
      val res: F[Response[F]] = for {
        board  <- req.req.as[BoardRequest]
        id     <- boardService.add(user.userId, board)
        result <- Ok(id.asJson)
      } yield result

      res.recoverWith {
        case e =>
          for {
            logger <- Slf4jLogger.create[F]
            _      <- logger.error(e)(e.getMessage)
            b      <- BadRequest()
          } yield b
      }

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

  val privateService: AuthedRoutes[User, F] = boardCreateService

}

object BoardServiceEndpoint {
  def apply[F[_]: Async](
    boardService: BoardService[F]
  )(implicit F: ConcurrentEffect[F]): BoardServiceEndpoint[F] = new BoardServiceEndpoint(boardService)
}
