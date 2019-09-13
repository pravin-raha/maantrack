package com.maantrack.domain.card
import cats.data.OptionT

trait CardRepository[F[_]] {

  def add(cardRequest: CardRequest): F[Long]

  def getById(id: Long): OptionT[F, Card]

  def deleteById(id: Long): F[Unit]

  def update(card: Card): F[Unit]
}
