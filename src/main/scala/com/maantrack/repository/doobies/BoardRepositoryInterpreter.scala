package com.maantrack.repository.doobies
import cats.data.OptionT
import cats.effect.Async
import com.maantrack.domain.board.{ Board, BoardRepository, BoardRequest }
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.log.LogHandler
import doobie.{ Fragments, Query0, Update0 }

object BoardSQL {
  import Fragments.whereAnd

  def byId(id: Long): Query0[Board] =
    (select ++ whereAnd(fr"board_id = $id"))
      .queryWithLogHandler[Board](LogHandler.jdkLogHandler)

  private def select: Fragment =
    fr"""
        select
             board_id, name, description, closed, organization_id, pinned, board_url, starred ,created_date, modified_date
        from board
      """

  def insert(board: BoardRequest): Update0 =
    sql"""
         insert into board
               (name, description, closed, organization_id, pinned, board_url,
               starred , created_date, modified_date)
         values
              ( ${board.name}, ${board.description}, ${board.closed}, ${board.organizationId}
              , ${board.pinned}, ${board.boardUrl}, ${board.starred}, ${board.createdDate}, ${board.modifiedDate})
       """.update

  def update(board: Board): Update0 =
    sql"""
         update board    
         set name = ${board.name}
         where board_id = ${board.boardId}
       """.update

  def delete(id: Long): Update0 =
    sql"""
         delete from board
         where board_id = $id
       """.update
}

class BoardRepositoryInterpreter[F[_]: Async](xa: HikariTransactor[F]) extends BoardRepository[F] {
  import BoardSQL._

  override def add(boardRequest: BoardRequest): F[Long] =
    insert(boardRequest)
      .withUniqueGeneratedKeys[Long]("board_id")
      .transact(xa)

  override def getById(id: Long): OptionT[F, Board] = OptionT(byId(id).option.transact(xa))

  override def deleteById(id: Long): F[Int] = delete(id).run.transact(xa)

  override def update(board: Board): F[Int] = BoardSQL.update(board).run.transact(xa)
}
