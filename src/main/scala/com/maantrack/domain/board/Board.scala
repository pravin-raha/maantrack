package com.maantrack.domain.board
import java.time.Instant

import cats.effect.Sync
import io.circe.generic.auto._
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

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

object BoardRequest {
  implicit def boardDecoder[F[_]: Sync]: EntityDecoder[F, BoardRequest] = jsonOf
}
