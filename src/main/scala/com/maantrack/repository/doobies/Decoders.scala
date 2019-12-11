package com.maantrack.repository.doobies

import java.time.Instant
import java.util.Date

import io.getquill.MappedEncoding

trait Decoders {
  implicit val instantDecoder: MappedEncoding[Date, Instant] = MappedEncoding[Date, java.time.Instant](_.toInstant)
}
