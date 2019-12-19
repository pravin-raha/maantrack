//package com.maantrack.endpoint
//
//import java.time.Instant
//
//import cats.effect.{ ContextShift, IO }
//import com.maantrack.Module
//import com.maantrack.domain.board.{ Board, BoardRequest }
//import com.maantrack.domain.user.{ Role, UserRequest }
//import com.maantrack.test.{ BaseTest, Requests, TestEmbeddedPostgres }
//import org.http4s.circe.CirceEntityCodec._
//import org.http4s.client.dsl.Http4sClientDsl
//import org.http4s.dsl.Http4sDsl
//import org.http4s.implicits._
//import org.http4s.server.Router
//import org.http4s.{ HttpApp, Uri }
//import org.scalatest.concurrent.Eventually
//
//import scala.concurrent.ExecutionContext
//
//class BoardTest extends BaseTest with TestEmbeddedPostgres with Eventually with Http4sClientDsl[IO] with Http4sDsl[IO] {
//
//  implicit var contextShift: ContextShift[IO] = _
//  var module: Module[IO, BCrypt]              = _
//
//  private lazy val boardRoutes: HttpApp[IO] = {
//    Router(("/board", module.boardServiceEndpoint)).orNotFound
//  }
//  private lazy val request = new Requests(module)
//
//  private val userRequest: UserRequest =
//    UserRequest("test@test.com", "fname", "lname", Role.Customer, "test", "test", Instant.now)
//
//  private val boardRequest: BoardRequest = BoardRequest(
//    "name",
//    Some("description"),
//    closed = false,
//    pinned = false,
//    "url",
//    starred = false
//  )
//
//  override protected def beforeAll(): Unit = {
//    super.beforeAll()
//    contextShift = IO.contextShift(ExecutionContext.global)
//    module = new Module(currentDb.xa, hasher)
//  }
//
//  "/board" should "create board" in {
//    (for {
//      loginResp          <- request.signUpAndLogIn(userRequest)
//      (_, authorization) = loginResp
//      postRequest        <- POST(boardRequest, Uri.unsafeFromString(s"/board"))
//      postRequestAuth    = postRequest.putHeaders(authorization.get)
//      postResponse       <- boardRoutes.run(postRequestAuth)
//    } yield {
//      postResponse.status shouldEqual Ok
//    }).unsafeRunSync
//  }
//
//  "/board" should "get board by boardId" in {
//    (for {
//      userRes            <- request.signUpAndLogIn(userRequest)
//      (_, authorization) = userRes
//      boardId            <- request.createAndGetBoard(authorization, boardRequest)
//      getRequest         <- GET(Uri.unsafeFromString(s"/board/$boardId"))
//      getRequestAuth     = getRequest.putHeaders(authorization.get)
//      getResponse        <- boardRoutes.run(getRequestAuth)
//      getBoard           <- getResponse.as[Board]
//    } yield {
//      getResponse.status shouldEqual Ok
//      getBoard.name shouldEqual boardRequest.name
//      getBoard.description shouldEqual boardRequest.description
//      getBoard.starred shouldEqual boardRequest.starred
//      getBoard.closed shouldEqual boardRequest.closed
//      getBoard.pinned shouldEqual boardRequest.pinned
//    }).unsafeRunSync
//  }
//
//  "/board" should "delete board by boardId" in {
//    (for {
//      userRes            <- request.signUpAndLogIn(userRequest)
//      (_, authorization) = userRes
//      boardId            <- request.createAndGetBoard(authorization, boardRequest)
//      deleteRequest      <- DELETE(Uri.unsafeFromString(s"/board/$boardId"))
//      deleteRequestAuth  = deleteRequest.putHeaders(authorization.get)
//      deleteResponse     <- boardRoutes.run(deleteRequestAuth)
//      getBoard           <- deleteResponse.as[Long]
//    } yield {
//      deleteResponse.status shouldEqual Ok
//      getBoard shouldEqual boardId
//    }).unsafeRunSync
//  }
//}
