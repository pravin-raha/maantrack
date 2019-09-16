package com.maantrack.endpoint

import cats.effect._
import cats.implicits._
import com.maantrack.domain.board.{ BoardRequest, BoardService }
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.Response
import tsec.authentication._

class BoardServiceEndpoint[F[_]: Sync](
  boardService: BoardService[F]
)(implicit F: ConcurrentEffect[F])
    extends Http4sDsl[F] {

  private val boardCreateService: AuthService[F] = TSecAuthService {
    case req @ POST -> Root asAuthed user =>
      val res: F[Response[F]] = for {
        board  <- req.request.as[BoardRequest]
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

    case GET -> Root / LongVar(boardId) asAuthed _ =>
      boardService.getById(boardId).value.flatMap {
        case Some(board) => Ok(board.asJson)
        case None        => NotFound(s"Board with board id $boardId not found".asJson)
      }

    case DELETE -> Root / LongVar(boardId) asAuthed _ =>
      boardService.deleteById(boardId).value.flatMap {
        case Some(board) => Ok(board.asJson)
        case None        => NotFound(s"Board with board id $boardId not found".asJson)
      }
  }

  val privateService: AuthService[F] = boardCreateService

}

object BoardServiceEndpoint {
  def apply[F[_]: Async](
    boardService: BoardService[F]
  )(implicit F: ConcurrentEffect[F]): BoardServiceEndpoint[F] = new BoardServiceEndpoint(boardService)
}
