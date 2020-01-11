package com.maantrack.repository.doobies

import cats.data.OptionT
import com.maantrack.domain.{ Card, CardRequest }

trait CardRepository[F[_]] {
  def add(cardRequest: CardRequest): F[Long]

  def getById(id: Long): OptionT[F, Card]

  def deleteById(id: Long): F[Long]

  def update(card: Card): F[Long]
}
