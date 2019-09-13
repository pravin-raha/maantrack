package com.maantrack.domain.board
import cats.data.OptionT

trait BoardRepository[F[_]] {

  def add(boardRequest: BoardRequest): F[Long]

  def getById(id: Long): OptionT[F, Board]

  def deleteById(id: Long): F[Unit]

  def update(board: Board): F[Unit]
}
