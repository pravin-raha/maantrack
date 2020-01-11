package com.maantrack.service

import cats.data.OptionT
import cats.effect.Async
import com.maantrack.domain.AppUserBoard
import com.maantrack.repository.doobies.AppUserBoardRepository

class AppUserBoardService[F[_]: Async](userBoardRepository: AppUserBoardRepository[F]) {
  def getById(id: Long): OptionT[F, AppUserBoard] = userBoardRepository.getById(id)

  def add(boardRequest: AppUserBoard): F[Unit] = userBoardRepository.add(boardRequest)

  def update(board: AppUserBoard): F[Int] = userBoardRepository.update(board)

  def deleteById(id: Long): F[Int] = userBoardRepository.deleteById(id)
}
