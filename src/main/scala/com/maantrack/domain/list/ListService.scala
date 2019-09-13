package com.maantrack.domain.list
import cats.effect.Async

class ListService[F[_]: Async](listRepository: ListRepository[F]) {

  def getById(id: Long) = listRepository.getById(id)

  def add(listRequest: ListRequest): F[Long] = listRepository.add(listRequest)

  def update(list: List): F[Unit] = listRepository.update(list)

  def deleteById(id: Long): F[Unit] = listRepository.deleteById(id)
}
