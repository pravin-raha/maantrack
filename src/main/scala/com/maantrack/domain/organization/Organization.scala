package com.maantrack.domain.organization

import java.time.Instant

import com.maantrack.domain.user.User

case class Organization(
  organizationId: Long,
  description: String,
  displayName: String,
  boardsIds: List[Long],
  name: String,
  organizationUrl: String,
  website: String,
  users: List[User],
  createdDate: Instant,
  modifiedDate: Instant
)

case class OrganizationRequest(
  description: String,
  displayName: String,
  boardsIds: List[Long],
  name: String,
  organizationUrl: String,
  website: String,
  users: List[User],
  createdDate: Instant,
  modifiedDate: Instant
)
