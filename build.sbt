lazy val commonSettings =
  Seq(
    organization := "io.github.pravin-raha",
    name := "maantrack",
    version := "0.1",
    scalaVersion := "2.13.2",
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

lazy val doobieVersion             = "0.12.1"
lazy val http4sVersion             = "0.21.19"
lazy val circeVersion              = "0.14.0-M4"
lazy val flywayVersion             = "6.5.1"
lazy val circeGenericExtrasVersion = "0.13.0"
lazy val circeConfigVersion        = "0.8.0"
lazy val refinedVersion            = "0.9.21"
lazy val catsVersion               = "2.4.2"
lazy val catsEffectVersion         = "2.3.1"
lazy val pureconfigVersion         = "0.14.1"
lazy val log4catsSlf4jVersion      = "1.1.1"
lazy val chimneyVersion            = "0.5.3"
lazy val scalaCheckVersion         = "1.15.3"
lazy val scalacticVersion          = "3.2.5"
lazy val scalaTestVersion          = "3.2.7"
lazy val scalaTestPlusVersion      = "3.1.0.0-RC2"
lazy val quillJdbcVersion          = "3.5.1"
lazy val http4sJwtAuthVersion      = "0.0.6"
lazy val jbcryptVersion            = "0.4"
lazy val logbackVersion            = "1.2.3"
lazy val swaggerUIVersion          = "3.43.0"
lazy val webjarsLocatorVersion     = "0.40"

lazy val doobie = Seq(
  "org.tpolecat" %% "doobie-core"      % doobieVersion,
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari"    % doobieVersion,
  "org.tpolecat" %% "doobie-postgres"  % doobieVersion,
  "org.tpolecat" %% "doobie-h2"        % doobieVersion,
  "org.tpolecat" %% "doobie-quill"     % doobieVersion,
  "io.getquill"  %% "quill-jdbc"       % quillJdbcVersion,
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
  "dev.profunktor" %% "http4s-jwt-auth"      % http4sJwtAuthVersion,
  "org.mindrot"    % "jbcrypt"               % jbcryptVersion
)

lazy val common = Seq(
  "org.typelevel"         %% "cats-core"      % catsVersion,
  "org.typelevel"         %% "cats-effect"    % catsEffectVersion,
  "com.github.pureconfig" %% "pureconfig"     % pureconfigVersion,
  "io.chrisdavenport"     %% "log4cats-slf4j" % log4catsSlf4jVersion,
  "ch.qos.logback"        % "logback-classic" % logbackVersion,
  "io.scalaland"          %% "chimney"        % chimneyVersion
)

lazy val refined = Seq(
  "eu.timepit" %% "refined" % refinedVersion
)

lazy val webjar = Seq(
  "org.webjars" % "swagger-ui"      % swaggerUIVersion,
  "org.webjars" % "webjars-locator" % webjarsLocatorVersion
)

lazy val testDependencies = Seq(
  "org.scalacheck"    %% "scalacheck"               % scalaCheckVersion    % "test",
  "org.scalatest"     %% "scalatest"                % scalaTestVersion     % "test",
  "org.scalactic"     %% "scalactic"                % scalacticVersion     % "test",
  "org.scalatestplus" %% "scalatestplus-scalacheck" % scalaTestPlusVersion % "test"
)

lazy val dbTestingStack = Seq("com.opentable.components" % "otj-pg-embedded" % "0.13.3")
