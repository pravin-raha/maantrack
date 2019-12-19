package com.maantrack.endpoint

import java.time.Instant

import cats.effect._
import cats.implicits._
import com.maantrack.domain.Error
import com.maantrack.domain.user._
import io.chrisdavenport.log4cats.Logger
import io.circe.generic.auto._
import io.circe.syntax._
import io.scalaland.chimney.dsl._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{ AuthedRoutes, HttpRoutes, Response }
import pdi.jwt.{ Jwt, JwtAlgorithm }

class UserServiceEndpoint[F[_]: Sync: Logger](
  userService: UserService[F]
) extends Http4sDsl[F] {
  private val userCreateService = HttpRoutes.of[F] {
    case req @ POST -> Root =>
      for {
        userRequest <- req.as[UserRequest]
        hash        = userRequest.password //TODO add new hasher
        userRes <- userService
                    .addUser(userRequest.copy(password = hash))
                    .map(_.into[UserResponse].transform)
        result <- Ok(userRes.asJson)
      } yield result
  }

  private val uService: AuthedRoutes[User, F] = AuthedRoutes.of {
    case GET -> Root / LongVar(userId) as _ =>
      userService
        .getUserById(userId)
        .value
        .flatMap {
          case Some(user) => Ok(user.into[UserResponse].transform.asJson)
          case None       => NotFound(s"User with user id $userId not found".asJson)
        }
    case DELETE -> Root / LongVar(userId) as _ =>
      userService
        .deleteUserById(userId)
        .value
        .flatMap {
          case Some(user) => Ok(user.into[UserResponse].transform.asJson)
          case None       => NotFound(s"User with user id $userId not found".asJson)
        }
  }

  private val loginService: HttpRoutes[F] = HttpRoutes.of[F] {

    case _ @POST -> Root / "login" =>
      val res: F[Response[F]] = for {
//        userCredential <- req.as[UserCredential]
//        user <- userService
//                 .getUserByUserName(userCredential.userName)
//                 .toRight(Error.NotFound("Bad Credential"): Throwable)
//                 .value
//                 .flatMap(_.liftTo[F])
//        hash   = PasswordHash[A](user.password)
//        status <- hasher.checkpw(userCredential.password.getBytes, hash)
        resp <- /*if (status == Verified) */ Ok()
//               else Sync[F].raiseError[Response[F]](Error.BadLogin())
        tok = Jwt.encode(
          UserRequest("test@test.com", "fname", "lname", Role.Customer, "test", "test", Instant.now).asJson.toString(),
          "53cr3t",
          JwtAlgorithm.HS256
        )
      } yield {
        print(tok)
        resp.addCookie("token", tok)
      }

      res.recoverWith {
        case n: Error.NotFound => NotFound(n.msg)
        case e                 => Logger[F].error(e)(e.getMessage) *> BadRequest()
      }
  }

  val publicService: HttpRoutes[F]          = loginService <+> userCreateService
  val privateService: AuthedRoutes[User, F] = uService

}

object UserServiceEndpoint {
  def apply[F[_]: Sync: Logger](
    userService: UserService[F]
  )(
    implicit F: ConcurrentEffect[F]
  ): UserServiceEndpoint[F] =
    new UserServiceEndpoint(
      userService
    )
}
