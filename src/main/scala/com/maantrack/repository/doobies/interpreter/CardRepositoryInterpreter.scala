package com.maantrack.repository.doobies.interpreter

import cats.data.OptionT
import cats.effect.Sync
import cats.syntax.all._
import com.maantrack.db.{ Decoders, Encoders, Schema }
import com.maantrack.domain.{ Card, CardRequest }
import com.maantrack.repository.doobies.CardRepository
import doobie.implicits._
import doobie.quill.DoobieContext.Postgres
import doobie.util.transactor.Transactor
import org.typelevel.log4cats.Logger
import io.getquill.SnakeCase

class CardRepositoryInterpreter[F[_]: Sync: Logger](
  xa: Transactor[F],
  override val ctx: Postgres[SnakeCase] with Decoders with Encoders
) extends CardRepository[F]
    with Schema {
  import ctx._

  override def add(cardRequest: CardRequest): F[Long] =
    run(quote {
      query[Card].insert(lift(cardRequest.toCard)).returning(_.cardId)
    }).transact(xa)

  override def getById(id: Long): OptionT[F, Card] =
    OptionT(run(quote {
      query[Card].filter(_.cardId == lift(id))
    }).transact(xa).map(_.headOption))

  override def deleteById(id: Long): F[Long] =
    run(quote {
      query[Card].filter(_.cardId == lift(id)).delete
    }).transact(xa).as(id)

  override def update(card: Card): F[Long] =
    run(quote {
      query[Card].filter(_.cardId == lift(card.cardId)).update(lift(card))
    }).transact(xa).as(card.cardId)
}
