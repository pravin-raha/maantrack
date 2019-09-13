package com.maantrack.domain.organization
import cats.effect.Async

class OrganizationRepositoryService[F[_]: Async](organizationRepository: OrganizationRepository[F]) {

  def getById(id: Long) = organizationRepository.getById(id)

  def add(orgRequest: OrganizationRequest): F[Long] = organizationRepository.add(orgRequest)

  def update(organization: Organization): F[Unit] = organizationRepository.update(organization)

  def deleteById(id: Long): F[Unit] = organizationRepository.deleteById(id)
}
