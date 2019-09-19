package com.maantrack.repository.doobies

import cats.implicits._
import cats.data.OptionT
import cats.effect.Async
import com.maantrack.domain.card.{ Card, CardRepository, CardRequest }
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.log.LogHandler
import doobie.{ Fragments, Query0, Update0 }

object CardSQL {
  import Fragments.whereAnd

  def byId(id: Long): Query0[Card] =
    (select ++ whereAnd(fr"card_id = $id"))
      .queryWithLogHandler[Card](LogHandler.jdkLogHandler)

  private def select: Fragment =
    fr"""
        select
             card_id, closed, description , due, due_completed, board_id, list_id , name, pos , created_date, modified_date
        from card
      """

  def insert(cardReq: CardRequest): Update0 =
    sql"""
         insert into card
               (closed, description , due, due_completed, board_id, list_id , name, pos , created_date, modified_date)
         values
              (${cardReq.closed}, ${cardReq.description}, ${cardReq.due}, ${cardReq.dueCompleted}, ${cardReq.boardId},
               ${cardReq.listId}, ${cardReq.name}, ${cardReq.pos}, now(), now())
       """.update

  def update(card: Card): Update0 =
    sql"""
         update card    
         set name = ${card.name}
         where card_id = ${card.cardId}
       """.update

  def delete(id: Long): Update0 =
    sql"""
         delete from card
         where card_id = $id
       """.update
}

class CardRepositoryInterpreter[F[_]: Async](xa: HikariTransactor[F]) extends CardRepository[F] {
  import CardSQL._

  override def add(cardRequest: CardRequest): F[Long] =
    insert(cardRequest)
      .withUniqueGeneratedKeys[Long]("card_id")
      .transact(xa)

  override def getById(id: Long): OptionT[F, Card] = OptionT(byId(id).option.transact(xa))

  override def deleteById(id: Long): OptionT[F, Card] =
    getById(id)
      .semiflatMap(card => delete(id).run.transact(xa).as(card))

  override def update(card: Card): F[Int] = CardSQL.update(card).run.transact(xa)
}
