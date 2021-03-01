package com.maantrack.repository.doobies.interpreter

import cats.data.OptionT
import cats.effect.Sync
import cats.syntax.all._
import com.maantrack.db.{ Decoders, Encoders, Schema }
import com.maantrack.domain.{ AppUserBoard, Board, BoardRequest }
import com.maantrack.repository.doobies.BoardRepository
import doobie.implicits._
import doobie.quill.DoobieContext.Postgres
import doobie.util.transactor.Transactor
import org.typelevel.log4cats.Logger
import io.getquill.SnakeCase

class BoardRepositoryInterpreter[F[_]: Sync: Logger](
  xa: Transactor[F],
  override val ctx: Postgres[SnakeCase] with Decoders with Encoders
) extends BoardRepository[F]
    with Schema {
  import ctx._

  override def add(userId: Long, boardRequest: BoardRequest): F[Long] = {
    (for {
      id <- run(query[Board].insert(lift(boardRequest.toBoard)).returning(_.boardId))
      _  <- run(query[AppUserBoard].insert(lift(AppUserBoard(userId, id))))
    } yield id).transact(xa)
  }

  override def getById(id: Long): OptionT[F, Board] =
    OptionT(run(quote {
      query[Board].filter(_.boardId == lift(id))
    }).transact(xa).map(_.headOption))

  override def deleteById(id: Long): F[Long] =
    run(query[Board].filter(_.boardId == lift(id)).delete).transact(xa).as(id)

  override def update(board: Board): F[Long] =
    run(query[Board].filter(_.boardId == lift(board.boardId)).update(lift(board))).transact(xa).as(board.boardId)
}
