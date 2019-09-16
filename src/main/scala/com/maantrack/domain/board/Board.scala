package com.maantrack.domain.board
import java.time.Instant

case class Board(
  boardId: Long,
  name: String,
  description: Option[String],
  closed: Boolean,
  pinned: Boolean,
  boardUrl: String,
  starred: Boolean,
  createdDate: Instant,
  modifiedDate: Instant
)

case class BoardRequest(
  name: String,
  description: Option[String],
  closed: Boolean,
  pinned: Boolean,
  boardUrl: String,
  starred: Boolean
)
