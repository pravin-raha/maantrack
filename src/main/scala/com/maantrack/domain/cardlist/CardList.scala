package com.maantrack.domain.cardlist
import java.time.Instant

import cats.effect.Sync
import io.circe.generic.auto._
import org.http4s.circe.{ jsonEncoderOf, jsonOf }
import org.http4s.{ EntityDecoder, EntityEncoder }

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
) { self =>
  def toCardList: CardList = CardList(
    0,
    self.name,
    self.closed,
    self.boardId,
    self.pos,
    Instant.now(),
    Instant.now()
  )
}

object CardList {
  implicit def cardListDecoder[F[_]: Sync]: EntityDecoder[F, CardList] = jsonOf
}

object CardListRequest {
  implicit def cardListRequestDecoder[F[_]: Sync]: EntityDecoder[F, CardListRequest] = jsonOf
  implicit def cardListRequestEncoder[F[_]: Sync]: EntityEncoder[F, CardListRequest] = jsonEncoderOf
}
