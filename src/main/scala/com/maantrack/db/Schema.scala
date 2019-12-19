package com.maantrack.db

import java.time.Clock

import com.maantrack.domain.cardlist.CardList
import com.maantrack.domain.user.User
import doobie.quill.DoobieContext.Postgres
import io.getquill.SnakeCase
import pdi.jwt.{ Jwt, JwtAlgorithm, JwtClaim }

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

object Test extends App {
  implicit val clock: Clock = Clock.systemUTC
  private val token: String = Jwt.encode(JwtClaim({
    """{"user":1}"""
  }).issuedNow.expiresIn(10), "secretKey", JwtAlgorithm.HS512)
  val decode = Jwt.decode(token).toOption
  println(decode)
}
