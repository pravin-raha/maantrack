package com.maantrack.domain.organization
import cats.data.OptionT
import cats.effect.Async

class OrganizationService[F[_]: Async](organizationRepository: OrganizationRepository[F]) {

  def getById(id: Long): OptionT[F, Organization] = organizationRepository.getById(id)

  def add(orgRequest: OrganizationRequest): F[Long] = organizationRepository.add(orgRequest)

  def update(organization: Organization): F[Int] = organizationRepository.update(organization)

  def deleteById(id: Long): F[Int] = organizationRepository.deleteById(id)
}
