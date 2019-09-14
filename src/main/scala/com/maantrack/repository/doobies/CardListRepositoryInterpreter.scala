package com.maantrack.repository.doobies

import cats.data.OptionT
import cats.effect.Async
import com.maantrack.domain.cardlist.{ CardList, CardListRepository, CardListRequest }
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.log.LogHandler
import doobie.{ Fragments, Query0, Update0 }

object CardListSQL {
  import Fragments.whereAnd

  def byId(id: Long): Query0[CardList] =
    (select ++ whereAnd(fr"list_id = $id"))
      .queryWithLogHandler[CardList](LogHandler.jdkLogHandler)

  private def select: Fragment =
    fr"""
        select
             list_id, name, closed, board_id, pos, created_date, modified_date
        from list
      """

  def insert(listReq: CardListRequest): Update0 =
    sql"""
         insert into list
               (name, closed, pos, created_date, modified_date)
         values
              (${listReq.name}, ${listReq.closed}, ${listReq.pos}, ${listReq.createdDate}, ${listReq.modifiedDate})
       """.update

  def update(list: CardList): Update0 =
    sql"""
         update list    
         set name = ${list.name}
         where list_id = ${list.listId}
       """.update

  def delete(id: Long): Update0 =
    sql"""
         delete from list
         where list_id = $id
       """.update
}

class CardListRepositoryInterpreter[F[_]: Async](xa: HikariTransactor[F]) extends CardListRepository[F] {
  import CardListSQL._

  override def add(listRequest: CardListRequest): F[Long] =
    insert(listRequest)
      .withUniqueGeneratedKeys[Long]("list_id")
      .transact(xa)

  override def getById(id: Long): OptionT[F, CardList] = OptionT(byId(id).option.transact(xa))

  override def deleteById(id: Long): F[Int] = delete(id).run.transact(xa)

  override def update(cardList: CardList): F[Int] = CardListSQL.update(cardList).run.transact(xa)
}
