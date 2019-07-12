package com.maantrack.domain.organization

import com.maantrack.domain.member.Membership

case class Organization(
    organizationId: Long,
    description: String,
    displayName: String,
    boardsIds: List[Long],
    name: String,
    memberships: List[Membership]
)
