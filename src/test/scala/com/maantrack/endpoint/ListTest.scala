package com.maantrack.endpoint

import java.time.Instant

import cats.effect.{ ContextShift, IO }
import com.maantrack.Module
import com.maantrack.domain.board.BoardRequest
import com.maantrack.domain.cardlist.{ CardList, CardListRequest }
import com.maantrack.domain.user.{ Role, UserRequest }
import com.maantrack.test.{ BaseTest, Requests, TestEmbeddedPostgres }
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.{ HttpApp, Uri }
import org.scalatest.concurrent.Eventually
import tsec.passwordhashers.jca.BCrypt

import scala.concurrent.ExecutionContext

class ListTest extends BaseTest with TestEmbeddedPostgres with Eventually with Http4sClientDsl[IO] with Http4sDsl[IO] {

  implicit var contextShift: ContextShift[IO] = _
  var module: Module[IO, BCrypt]              = _

  private lazy val listRoutes: HttpApp[IO] = {
    Router(("/list", module.listEndpoint)).orNotFound
  }
  private lazy val request = new Requests(module)

  private val userRequest: UserRequest =
    UserRequest("test@test.com", "fname", "lname", Role.Customer, "test", "test", Instant.now)

  private val boardRequest: BoardRequest =
    BoardRequest("name", Some("description"), closed = false, pinned = false, "url", starred = false)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    contextShift = IO.contextShift(ExecutionContext.global)
    module = new Module(currentDb.xa, hasher)
  }

  "/list" should "create list" in {
    (for {
      userRes            <- request.signUpAndLogIn(userRequest)
      (_, authorization) = userRes
      boardId            <- request.createAndGetBoard(authorization, boardRequest)
      cardList           = CardListRequest("name", closed = false, boardId, 0)
      postRequest        <- POST(cardList, Uri.unsafeFromString(s"/list"))
      postRequestAuth    = postRequest.putHeaders(authorization.get)
      postResponse       <- listRoutes.run(postRequestAuth)
    } yield {
      postResponse.status shouldEqual Ok
    }).unsafeRunSync
  }

  "/list" should "get list by listId" in {
    (for {
      userRes            <- request.signUpAndLogIn(userRequest)
      (_, authorization) = userRes
      boardId            <- request.createAndGetBoard(authorization, boardRequest)
      cardListReq        = CardListRequest("name", closed = false, boardId, 0)
      listId             <- request.createAndGetCardList(authorization, cardListReq)
      getRequest         <- GET(Uri.unsafeFromString(s"/list/$listId"))
      getRequestAuth     = getRequest.putHeaders(authorization.get)
      getResponse        <- listRoutes.run(getRequestAuth)
      getCardList        <- getResponse.as[CardList]
    } yield {
      getResponse.status shouldEqual Ok
      getCardList.name shouldEqual cardListReq.name
      getCardList.closed shouldEqual cardListReq.closed
      getCardList.boardId shouldEqual cardListReq.boardId
      getCardList.pos shouldEqual cardListReq.pos
    }).unsafeRunSync
  }

  "/list" should "delete list by listId" in {
    (for {
      userRes            <- request.signUpAndLogIn(userRequest)
      (_, authorization) = userRes
      boardId            <- request.createAndGetBoard(authorization, boardRequest)
      cardListReq        = CardListRequest("name", closed = false, boardId, 0)
      listId             <- request.createAndGetCardList(authorization, cardListReq)
      getRequest         <- GET(Uri.unsafeFromString(s"/list/$listId"))
      getRequestAuth     = getRequest.putHeaders(authorization.get)
      getResponse        <- listRoutes.run(getRequestAuth)
      getCardList        <- getResponse.as[CardList]
    } yield {
      getResponse.status shouldEqual Ok
      getCardList.name shouldEqual cardListReq.name
      getCardList.closed shouldEqual cardListReq.closed
      getCardList.boardId shouldEqual cardListReq.boardId
      getCardList.pos shouldEqual cardListReq.pos
    }).unsafeRunSync
  }
}
