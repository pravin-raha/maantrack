//package com.maantrack.test
//
//import cats.effect.concurrent.MVar
//import cats.effect.{ ContextShift, IO }
//import cats.implicits._
//import com.maantrack.config.DatabaseConfig
//import doobie.implicits._
//import doobie.util.transactor.Transactor
//import org.flywaydb.core.Flyway
//
//import scala.annotation.tailrec
//import scala.concurrent.ExecutionContext
//
//class TestDB(config: DatabaseConfig) {
//  implicit val cs: ContextShift[IO]             = IO.contextShift(ExecutionContext.global)
//  private val xaReady: MVar[IO, Transactor[IO]] = MVar.empty[IO, Transactor[IO]].unsafeRunSync()
//  private val done: MVar[IO, Unit]              = MVar.empty[IO, Unit].unsafeRunSync()
//
//  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
//    config.driver,
//    config.url,
//    config.user,
//    config.password
//  )
//
//  xaReady.put(xa) >> done.take
//
//  private val flyway = Flyway
//    .configure()
//    .dataSource(config.url, config.user, config.password)
//    .load()
//
//  @tailrec
//  final def connectAndMigrate(): Unit = {
//    try {
//      migrate()
//      testConnection()
//      println("Database migration & connection test complete")
//    } catch {
//      case _: Exception =>
//        println("Database not available, waiting 5 seconds to retry...")
//        Thread.sleep(5000)
//        connectAndMigrate()
//    }
//  }
//
//  def migrate(): Unit = {
//    flyway.migrate()
//    ()
//  }
//
//  def clean(): Unit = {
//    flyway.clean()
//  }
//
//  def testConnection(): Unit = {
//    sql"select 1".query[Int].unique.transact(xa).unsafeRunSync()
//    ()
//  }
//
//  def close(): Unit = {
//    done.put(()).unsafeRunSync()
//  }
//}
