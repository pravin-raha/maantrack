package com.maantrack.db

import java.time.Instant
import java.util.Date

import io.getquill.MappedEncoding

trait Decoders {
  implicit val instantDecoder: MappedEncoding[Date, Instant] = MappedEncoding[Date, Instant](_.toInstant)
}
