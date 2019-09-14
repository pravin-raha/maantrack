package com.maantrack.domain.cardlist
import java.time.Instant

case class CardList(
  listId: Long,
  name: String,
  closed: Boolean,
  boardId: Long,
  pos: Int,
  createdDate: Instant,
  modifiedDate: Instant
)

case class CardListRequest(
  name: String,
  closed: Boolean,
  boardId: Long,
  pos: Int,
  createdDate: Instant,
  modifiedDate: Instant
)
