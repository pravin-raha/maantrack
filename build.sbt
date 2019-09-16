name := "maantrack"

version := "0.1"

scalaVersion := "2.13.0"

lazy val doobieVersion        = "0.8.0-RC1"
lazy val http4sVersion        = "0.21.0-M4"
lazy val flywayDbVersion      = "5.2.4"
lazy val tsecVersion          = "0.2.0-M1"
lazy val circeVersion         = "0.12.1"
lazy val circeConfigVersion   = "0.7.0-M1"
lazy val refinedVersion       = "0.9.9"
lazy val catsVersion          = "2.0.0"
lazy val pureConfigVersion    = "0.11.1"
lazy val log4catsSlf4jVersion = "1.0.0"
lazy val chimneyVersion       = "0.3.2"
lazy val scalaCheckVersion    = "1.14.0"
lazy val scalaTestVersion     = "3.1.0-M2"
lazy val scalacticVersion     = "3.0.8"

libraryDependencies ++= http4s
libraryDependencies ++= tsec
libraryDependencies ++= doobie
libraryDependencies ++= common
libraryDependencies ++= refined
libraryDependencies ++= webjar
libraryDependencies ++= testDependencies

enablePlugins(JavaAppPackaging)

lazy val doobie = Seq(
  "org.tpolecat" %% "doobie-core"      % doobieVersion,
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari"    % doobieVersion,
  "org.tpolecat" %% "doobie-postgres"  % doobieVersion,
  "org.tpolecat" %% "doobie-h2"        % doobieVersion,
  "org.flywaydb" % "flyway-core"       % flywayDbVersion
)

lazy val http4s = Seq(
  "org.http4s" %% "http4s-dsl"           % http4sVersion,
  "org.http4s" %% "http4s-blaze-server"  % http4sVersion,
  "org.http4s" %% "http4s-circe"         % http4sVersion,
  "io.circe"   %% "circe-generic"        % circeVersion,
  "io.circe"   %% "circe-literal"        % circeVersion,
  "io.circe"   %% "circe-generic-extras" % circeVersion,
  "io.circe"   %% "circe-parser"         % circeVersion,
  "io.circe"   %% "circe-core"           % circeVersion,
  "io.circe"   %% "circe-config"         % circeConfigVersion
)

lazy val common = Seq(
  "mysql"                 % "mysql-connector-java" % "5.1.24",
  "org.typelevel"         %% "cats-core"           % catsVersion,
  "org.typelevel"         %% "cats-effect"         % catsVersion,
  "com.github.pureconfig" %% "pureconfig"          % pureConfigVersion,
  "io.chrisdavenport"     %% "log4cats-slf4j"      % log4catsSlf4jVersion,
  "ch.qos.logback"        % "logback-classic"      % "1.2.3",
  "io.scalaland"          %% "chimney"             % chimneyVersion
)

lazy val tsec = Seq(
  "io.github.jmcardon" %% "tsec-common"        % tsecVersion,
  "io.github.jmcardon" %% "tsec-password"      % tsecVersion,
  "io.github.jmcardon" %% "tsec-cipher-jca"    % tsecVersion,
  "io.github.jmcardon" %% "tsec-cipher-bouncy" % tsecVersion,
  "io.github.jmcardon" %% "tsec-mac"           % tsecVersion,
  "io.github.jmcardon" %% "tsec-signatures"    % tsecVersion,
  "io.github.jmcardon" %% "tsec-hash-jca"      % tsecVersion,
  "io.github.jmcardon" %% "tsec-hash-bouncy"   % tsecVersion,
  "io.github.jmcardon" %% "tsec-jwt-mac"       % tsecVersion,
  "io.github.jmcardon" %% "tsec-jwt-sig"       % tsecVersion,
  "io.github.jmcardon" %% "tsec-http4s"        % tsecVersion
)

lazy val refined = Seq(
  "eu.timepit" %% "refined" % refinedVersion
)

lazy val webjar = Seq(
  "org.webjars" % "webjars-locator" % "0.37",
  "org.webjars" % "swagger-ui"      % "3.23.5"
)

lazy val testDependencies = Seq(
  "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test",
  "org.scalatest"  %% "scalatest"  % scalaTestVersion  % "test",
  "org.scalactic"  %% "scalactic"  % scalacticVersion  % "test"
)

dockerExposedPorts ++= Seq(8080)

resolvers += Resolver.sonatypeRepo("releases")
resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"

sonarUseExternalConfig := true
coverageEnabled := false
coverageHighlighting := false

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
