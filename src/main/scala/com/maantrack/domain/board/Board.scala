package com.maantrack.domain.board
import java.time.Instant

case class Board(
  boardId: Long,
  name: String,
  description: String,
  closed: Boolean,
  organizationId: Long,
  pinned: Boolean,
  boardUrl: String,
  starred: Boolean,
  createdDate: Instant,
  modifiedDate: Instant
)
