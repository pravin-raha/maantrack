package com.maantrack.domain.user.board

import cats.data.OptionT

trait AppUserBoardRepository[F[_]] {
  def add(userBoard: AppUserBoard): F[Unit]

  def getById(id: Long): OptionT[F, AppUserBoard]

  def deleteById(id: Long): F[Int]

  def update(userBoard: AppUserBoard): F[Int]
}
