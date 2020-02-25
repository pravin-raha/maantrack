package com.maantrack.repository.doobies

import cats.data.OptionT
import com.maantrack.domain.{ CardList, CardListRequest }

trait CardListRepository[F[_]] {
  def add(listRequest: CardListRequest): F[Long]

  def getById(id: Long): OptionT[F, CardList]

  def deleteById(id: Long): F[Long]

  def update(card: CardList): F[Long]
}
