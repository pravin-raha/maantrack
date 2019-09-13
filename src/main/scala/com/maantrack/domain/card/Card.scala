package com.maantrack.domain.card
import java.time.Instant

case class Card(
  cardId: Long,
  closed: Boolean,
  description: String,
  due: Instant,
  dueCompleted: Boolean,
  boardId: Long,
  listId: Long,
  name: String,
  pos: Int,
  createdDate: Instant,
  modifiedDate: Instant
)

case class CardRequest(
  closed: Boolean,
  description: String,
  due: Instant,
  dueCompleted: Boolean,
  boardId: Long,
  listId: Long,
  name: String,
  pos: Int,
  createdDate: Instant,
  modifiedDate: Instant
)
