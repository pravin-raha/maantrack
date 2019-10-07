package com.maantrack.domain.card
import java.time.Instant

import cats.effect.Sync
import io.circe.generic.auto._
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

case class Card(
  cardId: Long,
  closed: Boolean,
  description: Option[String],
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
  description: Option[String],
  due: Instant,
  dueCompleted: Boolean,
  boardId: Long,
  listId: Long,
  name: String,
  pos: Int
)

object Card {
  implicit def card[F[_]: Sync]: EntityDecoder[F, Card] = jsonOf
}

object CardRequest {
  implicit def cardRequest[F[_]: Sync]: EntityDecoder[F, CardRequest] = jsonOf
}
