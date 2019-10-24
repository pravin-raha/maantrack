lazy val commonSettings =
  Seq(
    organization := "io.github.pravin-raha",
    name := "maantrack",
    version := "0.1",
    scalaVersion := "2.13.0",
    dockerExposedPorts ++= Seq(8080),
    resolvers += Resolver.sonatypeRepo("releases"),
    resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases",
    sonarUseExternalConfig := true,
    coverageEnabled := true,
    coverageHighlighting := true
  )

lazy val maantrack = project
  .in(file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= http4s,
    libraryDependencies ++= tsec,
    libraryDependencies ++= doobie,
    libraryDependencies ++= common,
    libraryDependencies ++= refined,
    libraryDependencies ++= webjar,
    libraryDependencies ++= testDependencies,
    libraryDependencies ++= dbTestingStack
  )
  .settings(
    addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt"),
    addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
  )

lazy val doobieVersion             = "0.8.4"
lazy val http4sVersion             = "0.21.0-M5"
lazy val flywayVersion             = "6.0.7"
lazy val tsecVersion               = "0.2.0-M2"
lazy val circeVersion              = "0.12.2"
lazy val circeGenericExtrasVersion = "0.12.2"
lazy val circeConfigVersion        = "0.7.0"
lazy val refinedVersion            = "0.9.10"
lazy val catsVersion               = "2.0.0"
lazy val catsEffectVersion         = "2.0.0"
lazy val pureconfigVersion         = "0.12.1"
lazy val log4catsSlf4jVersion      = "1.0.1"
lazy val chimneyVersion            = "0.3.3"
lazy val scalaCheckVersion         = "1.14.0"
lazy val scalaTestVersion          = "3.1.0-M2"
lazy val scalacticVersion          = "3.0.8"
lazy val scalaTestPlusVersion      = "3.1.0.0-RC2"

lazy val doobie = Seq(
  "org.tpolecat" %% "doobie-core"      % doobieVersion,
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari"    % doobieVersion,
  "org.tpolecat" %% "doobie-postgres"  % doobieVersion,
  "org.tpolecat" %% "doobie-h2"        % doobieVersion,
  "org.flywaydb" % "flyway-core"       % flywayVersion
)

lazy val http4s = Seq(
  "org.http4s" %% "http4s-dsl"           % http4sVersion,
  "org.http4s" %% "http4s-blaze-server"  % http4sVersion,
  "org.http4s" %% "http4s-circe"         % http4sVersion,
  "org.http4s" %% "http4s-blaze-client"  % http4sVersion % "test",
  "io.circe"   %% "circe-generic"        % circeVersion,
  "io.circe"   %% "circe-literal"        % circeVersion,
  "io.circe"   %% "circe-generic-extras" % circeGenericExtrasVersion,
  "io.circe"   %% "circe-parser"         % circeVersion,
  "io.circe"   %% "circe-core"           % circeVersion,
  "io.circe"   %% "circe-config"         % circeConfigVersion
)

lazy val common = Seq(
  "mysql"                 % "mysql-connector-java" % "8.0.18",
  "org.typelevel"         %% "cats-core"           % catsVersion,
  "org.typelevel"         %% "cats-effect"         % catsEffectVersion,
  "com.github.pureconfig" %% "pureconfig"          % pureconfigVersion,
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
  "org.scalacheck"    %% "scalacheck"               % scalaCheckVersion    % "test",
  "org.scalatest"     %% "scalatest"                % scalaTestVersion     % "test",
  "org.scalactic"     %% "scalactic"                % scalacticVersion     % "test",
  "org.scalatestplus" %% "scalatestplus-scalacheck" % scalaTestPlusVersion % "test"
)

lazy val dbTestingStack = Seq("com.opentable.components" % "otj-pg-embedded" % "0.13.3")
