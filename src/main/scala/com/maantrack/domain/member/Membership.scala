package com.maantrack.domain.member

case class Membership(
    membershipId: Long,
    memberId: Long,
    memberType: String,
    confirmed: Boolean
)
