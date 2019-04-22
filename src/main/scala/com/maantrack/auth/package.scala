package com.maantrack

import tsec.authentication.TSecBearerToken

package object auth {

  type BearerToken = TSecBearerToken[Int]
  val BearerToken: TSecBearerToken.type = TSecBearerToken

}
