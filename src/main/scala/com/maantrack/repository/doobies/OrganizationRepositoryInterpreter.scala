package com.maantrack.repository.doobies

import java.time.Instant

import cats.data.OptionT
import cats.effect.Sync
import com.maantrack.domain.organization.{ Organization, OrganizationRepository, OrganizationRequest }
import com.maantrack.domain.user.User
import com.maantrack.repository.doobies.Doobie._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor
import doobie.{ Fragments, Update0 }
import io.chrisdavenport.log4cats.Logger

object OrganizationSQL {
  import Fragments.whereAnd

  def byId(id: Long): doobie.ConnectionIO[Option[Organization]] =
    select(select ++ whereAnd(fr"organization_id = $id"))
      .map(_.headOption)

  def select(f: Fragment): ConnectionIO[List[Organization]] =
    f.query[(Long, String, String, String, String, String, Instant, Instant, Long, User)]
      .stream
      .compile
      .toList
      .map(
        _.groupMap(d => (d._1, d._2, d._3, d._4, d._5, d._6, d._7, d._8))(v => (v._9, v._10)).toList.map {
          case (k, v) => Organization(k._1, k._2, k._3, v.map(_._1), k._4, k._5, k._6, v.map(_._2), k._7, k._8)
        }
      )

  private val select: Fragment =
    fr"""
        select
         organization_id ,description ,display_name, name, organization_url, website, created_date, modified_date
        from board
      """

  def insert(orgReq: OrganizationRequest): Update0 =
    sql"""
         insert into board
               (description ,display_name, name, organization_url, website, created_date, modified_date)
         values
              (${orgReq.description}, ${orgReq.displayName}, ${orgReq.name}, ${orgReq.organizationUrl} ,
               ${orgReq.website},${orgReq.createdDate},${orgReq.modifiedDate})
       """.update(Doobie.doobieLogHandler)

  def update(org: Organization): Update0 =
    sql"""
         update organization
         set name = ${org.name}
         where organization_id = ${org.organizationId}
       """.update

  def delete(id: Long): Update0 =
    sql"""
         delete from organization
         where organization_id = $id
       """.update
}

class OrganizationRepositoryInterpreter[F[_]: Sync: Logger](xa: Transactor[F]) extends OrganizationRepository[F] {
  import OrganizationSQL._

  override def add(orgRequest: OrganizationRequest): F[Long] =
    insert(orgRequest)
      .withUniqueGeneratedKeys[Long]("organization_id")
      .transact(xa)

  override def getById(id: Long): OptionT[F, Organization] = OptionT(byId(id).transact(xa))

  override def deleteById(id: Long): F[Int] = delete(id).run.transact(xa)

  override def update(org: Organization): F[Int] = OrganizationSQL.update(org).run.transact(xa)
}
