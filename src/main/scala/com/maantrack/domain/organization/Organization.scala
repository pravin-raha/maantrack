package com.maantrack.domain.organization

import com.maantrack.domain.user.User

case class Organization(
  organizationId: Long,
  description: String,
  displayName: String,
  boardsIds: List[Long],
  name: String,
  users: List[User]
)
