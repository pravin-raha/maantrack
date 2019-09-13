package com.maantrack.domain.list
import java.time.Instant

case class List(
  listId: Long,
  name: String,
  closed: Boolean,
  boardId: Long,
  pos: Int,
  createdDate: Instant,
  modifiedDate: Instant
)

case class ListRequest(
  name: String,
  closed: Boolean,
  boardId: Long,
  pos: Int,
  createdDate: Instant,
  modifiedDate: Instant
)
