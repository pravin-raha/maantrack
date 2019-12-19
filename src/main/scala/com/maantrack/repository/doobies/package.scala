package com.maantrack.repository

import java.time.Instant

import doobie.enum.SqlState
import doobie.util.Meta

package object doobies {

  val UNIQUE_VIOLATION: SqlState =
    doobie.postgres.sqlstate.class23.UNIQUE_VIOLATION

  implicit val DateTimeMeta: Meta[Instant] =
    Meta[java.sql.Timestamp].imap(_.toInstant)(java.sql.Timestamp.from)
}
