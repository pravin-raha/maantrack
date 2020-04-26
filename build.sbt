lazy val commonSettings =
  Seq(
    organization := "io.github.pravin-raha",
    name := "maantrack",
    version := "0.1",
    scalaVersion := "2.13.0",
    dockerExposedPorts ++= Seq(8080),
    resolvers += Resolver.sonatypeRepo("releases"),
    resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases",
    sonarUseExternalConfig := false,
    coverageEnabled := false,
    coverageHighlighting := false
  )

lazy val maantrack = project
  .in(file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= http4s,
    libraryDependencies ++= doobie,
    libraryDependencies ++= common,
    libraryDependencies ++= refined,
    libraryDependencies ++= webjar,
    libraryDependencies ++= testDependencies,
    libraryDependencies ++= dbTestingStack
  )
  .settings(
    addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt"),
    addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck"),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")
  )

lazy val doobieVersion             = "0.9.0"
lazy val http4sVersion             = "0.21.3"
lazy val circeVersion              = "0.13.0"
lazy val flywayVersion             = "6.4.0"
lazy val circeGenericExtrasVersion = "0.13.0"
lazy val circeConfigVersion        = "0.8.0"
lazy val refinedVersion            = "0.9.14"
lazy val catsVersion               = "2.1.1"
lazy val catsEffectVersion         = "2.1.3"
lazy val pureconfigVersion         = "0.12.3"
lazy val log4catsSlf4jVersion      = "1.0.1"
lazy val chimneyVersion            = "0.5.0"
lazy val scalaCheckVersion         = "1.14.3"
lazy val scalaTestVersion          = "3.1.1"
lazy val scalacticVersion          = "3.1.1"
lazy val scalaTestPlusVersion      = "3.1.0.0-RC2"

lazy val doobie = Seq(
  "org.tpolecat" %% "doobie-core"      % doobieVersion,
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari"    % doobieVersion,
  "org.tpolecat" %% "doobie-postgres"  % doobieVersion,
  "org.tpolecat" %% "doobie-h2"        % doobieVersion,
  "org.tpolecat" %% "doobie-quill"     % doobieVersion,
  "io.getquill"  %% "quill-jdbc"       % "3.5.1",
  "org.flywaydb" % "flyway-core"       % flywayVersion
)

lazy val http4s = Seq(
  "org.http4s"     %% "http4s-dsl"           % http4sVersion,
  "org.http4s"     %% "http4s-blaze-server"  % http4sVersion,
  "org.http4s"     %% "http4s-circe"         % http4sVersion,
  "org.http4s"     %% "http4s-blaze-client"  % http4sVersion % "test",
  "io.circe"       %% "circe-generic"        % circeVersion,
  "io.circe"       %% "circe-literal"        % circeVersion,
  "io.circe"       %% "circe-generic-extras" % circeGenericExtrasVersion,
  "io.circe"       %% "circe-parser"         % circeVersion,
  "io.circe"       %% "circe-core"           % circeVersion,
  "io.circe"       %% "circe-config"         % circeConfigVersion,
  "dev.profunktor" %% "http4s-jwt-auth"      % "0.0.4",
  "org.mindrot"    % "jbcrypt"               % "0.4"
)

lazy val common = Seq(
  "mysql"                 % "mysql-connector-java" % "8.0.19",
  "org.typelevel"         %% "cats-core"           % catsVersion,
  "org.typelevel"         %% "cats-effect"         % catsEffectVersion,
  "com.github.pureconfig" %% "pureconfig"          % pureconfigVersion,
  "io.chrisdavenport"     %% "log4cats-slf4j"      % log4catsSlf4jVersion,
  "ch.qos.logback"        % "logback-classic"      % "1.2.3",
  "io.scalaland"          %% "chimney"             % chimneyVersion
)

lazy val refined = Seq(
  "eu.timepit" %% "refined" % refinedVersion
)

lazy val webjar = Seq(
  "org.webjars" % "webjars-locator" % "0.39",
  "org.webjars" % "swagger-ui"      % "3.25.0"
)

lazy val testDependencies = Seq(
  "org.scalacheck"    %% "scalacheck"               % scalaCheckVersion    % "test",
  "org.scalatest"     %% "scalatest"                % scalaTestVersion     % "test",
  "org.scalactic"     %% "scalactic"                % scalacticVersion     % "test",
  "org.scalatestplus" %% "scalatestplus-scalacheck" % scalaTestPlusVersion % "test"
)

lazy val dbTestingStack = Seq("com.opentable.components" % "otj-pg-embedded" % "0.13.3")
