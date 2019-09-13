package com.maantrack.domain.card
import cats.effect.Async

class CardService[F[_]: Async](cardRepository: CardRepository[F]) {

  def getById(id: Long) = cardRepository.getById(id)

  def add(cardRequest: CardRequest): F[Long] = cardRepository.add(cardRequest)

  def update(card: Card): F[Unit] = cardRepository.update(card)

  def deleteById(id: Long): F[Unit] = cardRepository.deleteById(id)
}
