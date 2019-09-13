package com.maantrack.domain.organization

import com.maantrack.domain.user.User
import java.time.Instant

case class Organization(
  organizationId: Long,
  description: String,
  displayName: String,
  boardsIds: List[Long],
  name: String,
  users: List[User],
  createdDate: Instant,
  modifiedDate: Instant
)

case class OrganizationRequest(
  description: String,
  displayName: String,
  boardsIds: List[Long],
  name: String,
  users: List[User],
  createdDate: Instant,
  modifiedDate: Instant
)
