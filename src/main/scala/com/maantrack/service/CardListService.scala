package com.maantrack.service

import cats.data.OptionT
import cats.effect.Async
import com.maantrack.domain.{ CardList, CardListRequest }
import com.maantrack.repository.doobies.CardListRepository

class CardListService[F[_]: Async](cardListRepository: CardListRepository[F]) {
  def getById(id: Long): OptionT[F, CardList] = cardListRepository.getById(id)

  def add(listRequest: CardListRequest): F[Long] = cardListRepository.add(listRequest)

  def update(list: CardList): F[Long] = cardListRepository.update(list)

  def deleteById(id: Long): F[Long] = cardListRepository.deleteById(id)
}
