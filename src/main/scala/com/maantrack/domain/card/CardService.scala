package com.maantrack.domain.card
import cats.data.OptionT
import cats.effect.Async

class CardService[F[_]: Async](cardRepository: CardRepository[F]) {

  def getById(id: Long): OptionT[F, Card] = cardRepository.getById(id)

  def add(cardRequest: CardRequest): F[Long] = cardRepository.add(cardRequest)

  def update(card: Card): F[Int] = cardRepository.update(card)

  def deleteById(id: Long): OptionT[F, Card] = cardRepository.deleteById(id)
}
