package com.maantrack.domain.list
import cats.data.OptionT

trait ListRepository[F[_]] {

  def add(listRequest: ListRequest): F[Long]

  def getById(id: Long): OptionT[F, List]

  def deleteById(id: Long): F[Unit]

  def update(card: List): F[Unit]
}
