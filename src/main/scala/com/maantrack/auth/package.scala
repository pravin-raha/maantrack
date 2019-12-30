package com.maantrack

import tsec.authentication.TSecBearerToken

package object auth {
  type Instant = java.time.Instant

  type BearerToken = TSecBearerToken[Long]
  val BearerToken: TSecBearerToken.type = TSecBearerToken
}
