package com.maantrack.domain.organization
import cats.data.OptionT

trait OrganizationRepository[F[_]] {

  def add(organizationRequest: OrganizationRequest): F[Long]

  def getById(id: Long): OptionT[F, Organization]

  def deleteById(id: Long): F[Unit]

  def update(organization: Organization): F[Unit]
}
