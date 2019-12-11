package com.maantrack.repository.doobies

import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import com.maantrack.domain.cardlist.{ CardList, CardListRepository, CardListRequest }
import com.maantrack.repository.doobies.Doobie._
import doobie.Update0
import doobie.implicits._
import doobie.quill.DoobieContext.Postgres
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor
import io.chrisdavenport.log4cats.Logger
import io.getquill.SnakeCase

object CardListSQL {

  private val tableName: Fragment = Fragment.const("list")

  def insert(listReq: CardListRequest): Update0 =
    (fr"insert into" ++ tableName ++
      fr"(name, closed, board_id, pos, created_date, modified_date)" ++
      fr"values (${listReq.name}, ${listReq.closed}, ${listReq.boardId}, ${listReq.pos}, now(), now())").update

  def update(list: CardList): Update0 =
    sql"""
         update list    
         set name = ${list.name}
         where list_id = ${list.listId}
       """.update

  def delete(id: Long): Update0 =
    (fr"delete from" ++ tableName ++ fr"where list_id = $id").update
}

class CardListRepositoryInterpreter[F[_]: Sync: Logger](
  xa: Transactor[F],
  override val ctx: Postgres[SnakeCase] with Decoders
) extends CardListRepository[F]
    with Schema {
  import CardListSQL._
  import ctx._

  override def add(listRequest: CardListRequest): F[Long] =
    insert(listRequest)
      .withUniqueGeneratedKeys[Long]("list_id")
      .transact(xa)

  override def getById(id: Long): OptionT[F, CardList] =
    OptionT(
      run(quote {
        cardListSchema.filter(cl => cl.listId == lift(id))
      }).map(_.headOption)
        .transact(xa)
    )

  override def deleteById(id: Long): OptionT[F, CardList] =
    getById(id)
      .semiflatMap(list => delete(id).run.transact(xa).as(list))

  override def update(cardList: CardList): F[Int] = CardListSQL.update(cardList).run.transact(xa)
}
