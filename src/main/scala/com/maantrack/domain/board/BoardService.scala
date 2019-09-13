package com.maantrack.domain.board

import cats.data.OptionT
import cats.effect.Async

class BoardService[F[_]: Async](boardRepository: BoardRepository[F]) {

  def getById(id: Long): OptionT[F, Board] = boardRepository.getById(id)

  def add(boardRequest: BoardRequest): F[Long] = boardRepository.add(boardRequest)

  def update(board: Board): F[Int] = boardRepository.update(board)

  def deleteById(id: Long): F[Int] = boardRepository.deleteById(id)
}
