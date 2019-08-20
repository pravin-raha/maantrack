package com.maantrack.domain.member

case class Member(
  memberId: Long,
  bio: String,
  email: String,
  confirmedEmail: Boolean,
  fullName: String,
  boardIds: List[Long],
  orgAdminIds: List[Long],
  organizationIds: List[Long],
  username: String,
  url: String,
  uploadedAvatarUrl: String
)
