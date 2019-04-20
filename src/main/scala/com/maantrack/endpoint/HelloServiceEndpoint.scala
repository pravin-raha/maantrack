package com.maantrack.endpoint

import cats.effect.{Async, ConcurrentEffect}
import cats.implicits._
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes, Response}
import tsec.authentication.{
  BackingStore,
  BearerTokenAuthenticator,
  SecuredRequestHandler,
  TSecAuthService,
  TSecBearerToken,
  TSecTokenSettings,
  _
}
import tsec.common.SecureRandomId

import scala.concurrent.duration._
import scala.language.higherKinds

class HelloServiceEndpoint[F[_]: Async](implicit F: ConcurrentEffect[F])
    extends Http4sDsl[F] {

  import com.maantrack.auth.ExampleAuthHelpers._

  type AuthService = TSecAuthService[User, TSecBearerToken[Int], F]

  val bearerTokenStore: BackingStore[F, SecureRandomId, TSecBearerToken[Int]] =
    dummyBackingStore[F, SecureRandomId, TSecBearerToken[Int]](
      s => SecureRandomId.coerce(s.id)
    )
  //We create a way to store our users. You can attach this to say, your doobie accessor
  val userStore: BackingStore[F, Int, User] =
    dummyBackingStore[F, Int, User](_.id)

  val settings: TSecTokenSettings = TSecTokenSettings(
    expiryDuration = 10.minutes, //Absolute expiration time
    maxIdle = None
  )

  val bearerTokenAuth =
    BearerTokenAuthenticator(
      bearerTokenStore,
      userStore,
      settings
    )

  val Auth: SecuredRequestHandler[F, Int, User, TSecBearerToken[Int]] =
    SecuredRequestHandler[F, Int, User, TSecBearerToken[Int]](bearerTokenAuth)

  val authService1: AuthService = TSecAuthService {
    //Where user is the case class User above
    case _ @GET -> Root / "api" asAuthed _ =>
      /*
      Note: The request is of type: SecuredRequest, which carries:
      1. The request
      2. The Authenticator (i.e token)
      3. The identity (i.e in this case, User)
       */
      Ok()
  }

  val authedService2: AuthService = TSecAuthService {
    case GET -> Root / "api2" asAuthed _ =>
      Ok()
  }

  val lifted: HttpRoutes[F] = Auth.liftService(authService1)
  val liftedComposed: HttpRoutes[F] =
    Auth.liftService(authService1 <+> authedService2)

  implicit val elementRequestDecoder: EntityDecoder[F, UserRequest] =
    jsonOf[F, UserRequest]
  val helloservice: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "hello" / name =>
      Ok(s"Hello, $name.")

    case req @ POST -> Root / "user" =>
      val res: F[Response[F]] = for {
        userRequest <- req.as[UserRequest]
        _ <- userStore.put(
          User(userRequest.id, userRequest.age, userRequest.name)
        )
        result <- Ok()
      } yield result

      res.recoverWith { case _ => BadRequest() }

    case req @ POST -> Root / "login" =>
      val res: F[Response[F]] = for {
        userRequest <- req.as[UserRequest]
        resp <- Ok()
        tok <- bearerTokenAuth.create(userRequest.id)
      } yield bearerTokenAuth.embed(resp, tok)
      res.recoverWith { case _ => BadRequest() }
  }

  def service: HttpRoutes[F] = helloservice <+> liftedComposed

}

object HelloServiceEndpoint {
  def apply[F[_]: Async](
      implicit F: ConcurrentEffect[F]
  ): HelloServiceEndpoint[F] =
    new HelloServiceEndpoint()
}
