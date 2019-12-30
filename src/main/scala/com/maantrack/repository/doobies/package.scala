package com.maantrack.repository

import java.time.Instant

import doobie.enum.SqlState
import doobie.util.Meta
import tsec.common.SecureRandomId

package object doobies {
  val UNIQUE_VIOLATION: SqlState =
    doobie.postgres.sqlstate.class23.UNIQUE_VIOLATION

  implicit val secureRandomIdMeta: Meta[SecureRandomId] =
    Meta[Array[Byte]].imap(x => SecureRandomId.apply(new String(x)))(
      _.getBytes
    )

  implicit val DateTimeMeta: Meta[Instant] =
    Meta[java.sql.Timestamp].imap(_.toInstant)(java.sql.Timestamp.from)
}
