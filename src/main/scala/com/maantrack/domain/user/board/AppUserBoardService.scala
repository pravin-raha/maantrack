package com.maantrack.domain.user.board

import cats.data.OptionT
import cats.effect.Async

class AppUserBoardService[F[_]: Async](userBoardRepository: AppUserBoardRepository[F]) {
  def getById(id: Long): OptionT[F, AppUserBoard] = userBoardRepository.getById(id)

  def add(boardRequest: AppUserBoard): F[Unit] = userBoardRepository.add(boardRequest)

  def update(board: AppUserBoard): F[Int] = userBoardRepository.update(board)

  def deleteById(id: Long): F[Int] = userBoardRepository.deleteById(id)
}
