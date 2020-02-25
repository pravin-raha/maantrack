package com.maantrack.db

import com.maantrack.domain.{ CardList, User }
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

  val userSchema: Quoted[EntityQuery[User]] = quote {
    querySchema[User](
      "app_user",
      _.userType.roleRepr -> "user_type",
      _.userId            -> "app_user_id"
    )
  }
}
