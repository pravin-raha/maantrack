package com.maantrack.domain.board

import cats.effect.Async

class BoardService[F[_]: Async](boardRepository: BoardRepository[F]) {

  def getById(id: Long) = boardRepository.getById(id)

  def add(boardRequest: BoardRequest): F[Long] = boardRepository.add(boardRequest)

  def update(board: Board): F[Unit] = boardRepository.update(board)

  def deleteById(id: Long): F[Unit] = boardRepository.deleteById(id)
}
