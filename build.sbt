name := "maantrack"

version := "0.1"

scalaVersion := "2.13.0"

lazy val doobieVersion      = "0.8.0-RC1"
lazy val http4sVersion      = "0.21.0-M4"
lazy val flywayDb           = "5.2.4"
lazy val tsecV              = "0.2.0-M1"
lazy val circeV             = "0.12.0-RC3"
lazy val CirceConfigVersion = "0.7.0-M1"
lazy val refinedV           = "0.9.9"
lazy val catsV              = "2.0.0-RC1"
lazy val pureConfigV        = "0.11.1"
lazy val log4catsSlf4j      = "1.0.0-RC1"
lazy val chimneyV           = "0.3.2"

libraryDependencies ++= http4s
libraryDependencies ++= tsec
libraryDependencies ++= doobie
libraryDependencies ++= common
libraryDependencies ++= refined

lazy val doobie = Seq(
  "org.tpolecat" %% "doobie-core"     % doobieVersion,
  "org.tpolecat" %% "doobie-specs2"   % doobieVersion,
  "org.tpolecat" %% "doobie-hikari"   % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-h2"       % doobieVersion,
  "org.flywaydb" % "flyway-core"      % flywayDb
)

lazy val http4s = Seq(
  "org.http4s" %% "http4s-dsl"           % http4sVersion,
  "org.http4s" %% "http4s-blaze-server"  % http4sVersion,
  "org.http4s" %% "http4s-blaze-client"  % http4sVersion,
  "org.http4s" %% "http4s-circe"         % http4sVersion,
  "io.circe"   %% "circe-generic"        % circeV,
  "io.circe"   %% "circe-literal"        % circeV,
  "io.circe"   %% "circe-generic-extras" % circeV,
  "io.circe"   %% "circe-parser"         % circeV,
  "io.circe"   %% "circe-core"           % circeV,
  "io.circe"   %% "circe-config"         % CirceConfigVersion
)

lazy val common = Seq(
  "mysql"                 % "mysql-connector-java" % "5.1.24",
  "org.typelevel"         %% "cats-core"           % catsV,
  "org.typelevel"         %% "cats-effect"         % catsV,
  "com.github.pureconfig" %% "pureconfig"          % pureConfigV,
  "io.chrisdavenport"     %% "log4cats-slf4j"      % log4catsSlf4j,
  "ch.qos.logback"        % "logback-classic"      % "1.2.3",
  "io.scalaland"          %% "chimney"             % chimneyV
)

lazy val tsec = Seq(
  "io.github.jmcardon" %% "tsec-common"        % tsecV,
  "io.github.jmcardon" %% "tsec-password"      % tsecV,
  "io.github.jmcardon" %% "tsec-cipher-jca"    % tsecV,
  "io.github.jmcardon" %% "tsec-cipher-bouncy" % tsecV,
  "io.github.jmcardon" %% "tsec-mac"           % tsecV,
  "io.github.jmcardon" %% "tsec-signatures"    % tsecV,
  "io.github.jmcardon" %% "tsec-hash-jca"      % tsecV,
  "io.github.jmcardon" %% "tsec-hash-bouncy"   % tsecV,
  "io.github.jmcardon" %% "tsec-jwt-mac"       % tsecV,
  "io.github.jmcardon" %% "tsec-jwt-sig"       % tsecV,
  "io.github.jmcardon" %% "tsec-http4s"        % tsecV
)

lazy val refined = Seq(
  "eu.timepit" %% "refined" % refinedV
)
