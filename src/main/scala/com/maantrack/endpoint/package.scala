package com.maantrack

import com.maantrack.domain.user.User
import tsec.authentication.{ TSecAuthService, TSecBearerToken }

package object endpoint {

  type AuthService[F[_]] = TSecAuthService[User, TSecBearerToken[Long], F]

}
