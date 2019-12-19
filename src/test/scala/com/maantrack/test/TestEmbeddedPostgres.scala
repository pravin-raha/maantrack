//package com.maantrack.test
//
//import com.maantrack.config.DatabaseConfig
//import com.opentable.db.postgres.embedded.EmbeddedPostgres
//import org.postgresql.jdbc.PgConnection
//import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach, Suite }
//
///**
// * Base trait for tests which use the database. The database is cleaned after each test.
// */
//trait TestEmbeddedPostgres extends BeforeAndAfterEach with BeforeAndAfterAll { self: Suite =>
//  private var postgres: EmbeddedPostgres      = _
//  private var currentDbConfig: DatabaseConfig = _
//  var currentDb: TestDB                       = _
//
//  override protected def beforeAll(): Unit = {
//    super.beforeAll()
//    postgres = EmbeddedPostgres.builder().start()
//    postgres.getPostgresDatabase.getConnection.asInstanceOf[PgConnection].setPrepareThreshold(100)
//    currentDbConfig = DatabaseConfig(
//      postgres.getJdbcUrl("postgres", "postgres"),
//      "org.postgresql.Driver",
//      "postgres",
//      "postgres",
//      5,
//      "sPool"
//    )
//
//    currentDb = new TestDB(currentDbConfig)
//    currentDb.testConnection()
//  }
//
//  override protected def afterAll(): Unit = {
//    postgres.close()
//    currentDb.close()
//    super.afterAll()
//  }
//
//  override protected def beforeEach(): Unit = {
//    super.beforeEach()
//    currentDb.migrate()
//  }
//
//  override protected def afterEach(): Unit = {
//    currentDb.clean()
//    super.afterEach()
//  }
//}
