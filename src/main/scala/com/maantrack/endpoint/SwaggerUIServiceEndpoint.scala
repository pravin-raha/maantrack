package com.maantrack.endpoint

import cats.effect.{ Blocker, ConcurrentEffect, ContextShift, Sync }
import io.circe.Json
import org.http4s.dsl.Http4sDsl
import org.http4s.{ HttpRoutes, StaticFile }
import org.http4s.headers.Location
import org.webjars.WebJarAssetLocator
import org.http4s.implicits._
import org.http4s.circe._

class SwaggerUIServiceEndpoint[F[_]: Sync](blocker: Blocker)(implicit cf: ConcurrentEffect[F], cs: ContextShift[F])
    extends Http4sDsl[F] {
  private val swaggerUiPath = Path("swagger-ui")

  val service: HttpRoutes[F] = HttpRoutes.of[F] {
    case _ @GET -> `swaggerUiPath` / "config.json" =>
      //Entry point to Swagger UI
      Ok(Json.obj("url" -> Json.fromString(s"/swagger.yaml")))
    case _ @GET -> `swaggerUiPath` =>
      PermanentRedirect(Location(uri"swagger-ui/index.html"))
    case request @ GET -> path if path.startsWith(swaggerUiPath) =>
      //Serves Swagger UI files
      val file = "/" + path.toList.drop(swaggerUiPath.toList.size).mkString("/")
      (if (file == "/index.html") {
         StaticFile.fromResource("/swagger-ui/index.html", blocker, Some(request))
       } else {
         StaticFile.fromResource(swaggerUiResources + file, blocker, Some(request))
       }).getOrElseF(NotFound())
    case request @ GET -> _ / "swagger.yaml" =>
      StaticFile.fromResource("/swagger.yaml", blocker, Some(request)).getOrElseF(NotFound())
  }

  private lazy val swaggerUiResources = s"/META-INF/resources/webjars/swagger-ui/$swaggerUiVersion"

  private lazy val swaggerUiVersion: String = {
    Option(new WebJarAssetLocator().getWebJars.get("swagger-ui")).fold {
      throw new RuntimeException(s"Could not detect swagger-ui webjar version")
    } { version =>
      version
    }
  }
}

object SwaggerUIServiceEndpoint {
  def apply[F[_]: Sync](
    blocker: Blocker
  )(implicit cf: ConcurrentEffect[F], cs: ContextShift[F]): SwaggerUIServiceEndpoint[F] =
    new SwaggerUIServiceEndpoint(blocker)
}
