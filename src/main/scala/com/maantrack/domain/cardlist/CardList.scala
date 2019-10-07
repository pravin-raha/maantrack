package com.maantrack.domain.cardlist
import java.time.Instant

import cats.effect.Sync
import io.circe.generic.auto._
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

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
  pos: Int
)

object CardListRequest {
  implicit def cardListRequest[F[_]: Sync]: EntityDecoder[F, CardListRequest] = jsonOf
}
