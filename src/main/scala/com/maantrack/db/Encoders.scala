package com.maantrack.db

import java.time.Instant
import java.util.Date

import io.getquill.MappedEncoding

trait Encoders {
  implicit val instanceEncoder: MappedEncoding[Instant, Date] = MappedEncoding[Instant, Date](Date.from)
}
