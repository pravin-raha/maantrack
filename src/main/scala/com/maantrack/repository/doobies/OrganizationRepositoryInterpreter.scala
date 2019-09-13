package com.maantrack.repository.doobies

import cats.data.OptionT
import cats.effect.Async
import com.maantrack.domain.organization.{ Organization, OrganizationRepository, OrganizationRequest }
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.log.LogHandler
import doobie.{ Fragments, Query0, Update0 }

object OrganizationSQL {
  import Fragments.whereAnd

  def byId(id: Long): Query0[Organization] =
    (select ++ whereAnd(fr"organization_id = $id"))
      .queryWithLogHandler[Organization](LogHandler.jdkLogHandler)

  private def select: Fragment =
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
       """.update

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

class OrganizationRepositoryInterpreter[F[_]: Async](xa: HikariTransactor[F]) extends OrganizationRepository[F] {
  import OrganizationSQL._

  override def add(orgRequest: OrganizationRequest): F[Long] =
    insert(orgRequest)
      .withUniqueGeneratedKeys[Long]("organization_id")
      .transact(xa)

  override def getById(id: Long): OptionT[F, Organization] = OptionT(byId(id).option.transact(xa))

  override def deleteById(id: Long): F[Int] = delete(id).run.transact(xa)

  override def update(org: Organization): F[Int] = OrganizationSQL.update(org).run.transact(xa)
}
