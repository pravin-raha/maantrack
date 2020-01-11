package com.maantrack.service

import cats.data.OptionT
import cats.effect.Async
import com.maantrack.domain.{ Card, CardRequest }
import com.maantrack.repository.doobies.CardRepository

class CardService[F[_]: Async](cardRepository: CardRepository[F]) {
  def getById(id: Long): OptionT[F, Card] = cardRepository.getById(id)

  def add(cardRequest: CardRequest): F[Long] = cardRepository.add(cardRequest)

  def update(card: Card): F[Long] = cardRepository.update(card)

  def deleteById(id: Long): F[Long] = cardRepository.deleteById(id)
}
