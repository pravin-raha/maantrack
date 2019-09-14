package com.maantrack.domain.cardlist
import cats.data.OptionT
import cats.effect.Async

class CardListService[F[_]: Async](cardListRepository: CardListRepository[F]) {

  def getById(id: Long): OptionT[F, CardList] = cardListRepository.getById(id)

  def add(listRequest: CardListRequest): F[Long] = cardListRepository.add(listRequest)

  def update(list: CardList): F[Int] = cardListRepository.update(list)

  def deleteById(id: Long): F[Int] = cardListRepository.deleteById(id)
}
