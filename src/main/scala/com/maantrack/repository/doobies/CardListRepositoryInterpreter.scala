package com.maantrack.repository.doobies

import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import com.maantrack.db.{ Decoders, Encoders, Schema }
import com.maantrack.domain.cardlist.{ CardList, CardListRepository, CardListRequest }
import doobie.implicits._
import doobie.quill.DoobieContext.Postgres
import doobie.util.transactor.Transactor
import io.chrisdavenport.log4cats.Logger
import io.getquill.SnakeCase

class CardListRepositoryInterpreter[F[_]: Sync: Logger](
  xa: Transactor[F],
  override val ctx: Postgres[SnakeCase] with Decoders with Encoders
) extends CardListRepository[F]
    with Schema {
  import ctx._

  private def selectCardListById(id: Long): ctx.Quoted[ctx.EntityQuery[CardList]] = quote {
    cardListSchema.filter(cl => cl.listId == lift(id))
  }
  override def add(listRequest: CardListRequest): F[Long] =
    run(quote {
      cardListSchema.insert(lift(listRequest.toCardList)).returningGenerated(_.listId)
    }).transact(xa)

  override def getById(id: Long): OptionT[F, CardList] =
    OptionT(
      run(selectCardListById(id))
        .map(_.headOption)
        .transact(xa)
    )

  override def deleteById(id: Long): F[Long] = run(selectCardListById(id).delete).transact(xa).as(id)

  override def update(cardList: CardList): F[Long] =
    run(quote {
      cardListSchema
        .filter(cl => cl.listId == lift(cardList.listId))
        .update(lift(cardList))
    }).transact(xa).as(cardList.listId) // Quill did not support update returning
}
