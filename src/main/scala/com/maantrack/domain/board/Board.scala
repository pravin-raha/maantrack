package com.maantrack.domain.board
import java.time.Instant

import cats.effect.Sync
import io.circe.generic.auto._
import io.scalaland.chimney.dsl._
import org.http4s.circe.{ jsonEncoderOf, jsonOf }
import org.http4s.{ EntityDecoder, EntityEncoder }

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
) {
  self =>

  def toBoard: Board =
    self
      .into[Board]
      .withFieldConst(_.boardId, 0L)
      .withFieldConst(_.createdDate, Instant.now())
      .withFieldConst(_.modifiedDate, Instant.now())
      .transform
}

object BoardRequest {
  implicit def boardRequestDecoder[F[_]: Sync]: EntityDecoder[F, BoardRequest] = jsonOf
  implicit def boardRequestEncoder[F[_]: Sync]: EntityEncoder[F, BoardRequest] = jsonEncoderOf

}

object Board {
  implicit def boardDecoder[F[_]: Sync]: EntityDecoder[F, Board] = jsonOf
  implicit def boardEncoder[F[_]: Sync]: EntityEncoder[F, Board] = jsonEncoderOf
}
