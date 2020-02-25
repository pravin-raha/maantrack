package com.maantrack.repository.doobies

import cats.data.OptionT
import com.maantrack.domain.{ Board, BoardRequest }

trait BoardRepository[F[_]] {
  def add(userId: Long, boardRequest: BoardRequest): F[Long]

  def getById(id: Long): OptionT[F, Board]

  def deleteById(id: Long): F[Long]

  def update(board: Board): F[Long]
}
