package com.maantrack.repository.doobies.interpreter

import cats.data.OptionT
import cats.effect.Sync
import cats.syntax.all._
import com.maantrack.domain.AppUserBoard
import com.maantrack.repository.doobies.AppUserBoardRepository
import com.maantrack.repository.doobies.Doobie._
import doobie.Fragments
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.query.Query0
import doobie.util.transactor.Transactor
import doobie.util.update.Update0
import org.typelevel.log4cats.Logger

object AppUserBoardSQL {
  import Fragments.whereAnd

  private val tableName = Fragment.const("app_user_board")

  private def whereIdAnd(id: Long): doobie.Fragment = whereAnd(fr"board_id = $id")

  def byId(id: Long): Query0[AppUserBoard] =
    (select ++ whereIdAnd(id))
      .query[AppUserBoard]

  private def select: Fragment = fr"select app_user_id, board_id from" ++ tableName

  def insert(appUserBoard: AppUserBoard): Update0 =
    (fr"insert into" ++ tableName ++ fr"(app_user_id, board_id) values ( ${appUserBoard.appUserId}, ${appUserBoard.boardId})").update

  def update(board: AppUserBoard): Update0 = ???

  def delete(id: Long): Update0 = (fr"delete from" ++ tableName ++ whereIdAnd(id)).update
}

class AppUserBoardRepositoryInterpreter[F[_]: Sync: Logger](xa: Transactor[F]) extends AppUserBoardRepository[F] {
  import AppUserBoardSQL._

  override def add(userBoard: AppUserBoard): F[Unit] =
    insert(userBoard).run
      .transact(xa)
      .map(_ => ())

  override def getById(id: Long): OptionT[F, AppUserBoard] = OptionT(byId(id).option.transact(xa))

  override def deleteById(id: Long): F[Int] = delete(id).run.transact(xa)

  override def update(appUserBoard: AppUserBoard): F[Int] = AppUserBoardSQL.update(appUserBoard).run.transact(xa)
}
