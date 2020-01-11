package com.maantrack.service

import cats.data.OptionT
import cats.effect.Async
import com.maantrack.domain.{ Board, BoardRequest }
import com.maantrack.repository.doobies.BoardRepository

class BoardService[F[_]: Async](boardRepository: BoardRepository[F]) {
  def getById(id: Long): OptionT[F, Board] = boardRepository.getById(id)

  def add(userId: Long, boardRequest: BoardRequest): F[Long] = boardRepository.add(userId, boardRequest)

  def update(board: Board): F[Long] = boardRepository.update(board)

  def deleteById(id: Long): F[Long] = boardRepository.deleteById(id)
}
