package com.maantrack.db

import com.maantrack.domain.cardlist.CardList
import doobie.quill.DoobieContext.Postgres
import io.getquill.SnakeCase

trait Schema {
  val ctx: Postgres[SnakeCase] with Decoders

  import ctx._

  val cardListSchema: Quoted[EntityQuery[CardList]] = quote {
    querySchema[CardList](
      "list"
    )
  }

}
