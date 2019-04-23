package com.maantrack.repository

import com.maantrack.auth.Instant
import doobie.enum.SqlState
import doobie.util.Meta
import tsec.common.SecureRandomId

package object doobies {
  implicit val instantMeta: Meta[Instant] = Meta[Instant]

  val UNIQUE_VIOLATION: SqlState =
    doobie.postgres.sqlstate.class23.UNIQUE_VIOLATION

  implicit val secureRandomIdMeta: Meta[SecureRandomId] =
    Meta[Array[Byte]].imap(
      x => SecureRandomId.apply(new String(x))
    )(
      _.getBytes
    )
}
