package com.maantrack.endpoint

import java.time.Instant

import cats.effect.{ Blocker, ContextShift, IO }
import com.maantrack.Module
import com.maantrack.domain._
import com.maantrack.test.{ BaseTest, Requests, TestEmbeddedPostgres }
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.{ AuthScheme, Credentials, HttpApp, Uri }
import org.scalatest.concurrent.Eventually

import scala.concurrent.ExecutionContext

class ListTest extends BaseTest with TestEmbeddedPostgres with Eventually with Http4sClientDsl[IO] with Http4sDsl[IO] {

  implicit var contextShift: ContextShift[IO] = _
  var module: Module[IO]                      = _

  private lazy val listRoutes: HttpApp[IO] = module.httpApp
  private lazy val request                 = new Requests(module)

  private val userRequest: UserRequest =
    UserRequest("test@test.com", "fname", "lname", Role.Customer, "test", "test", Instant.now)

  private val boardRequest: BoardRequest =
    BoardRequest("name", Some("description"), closed = false, pinned = false, "url", starred = false)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    contextShift = IO.contextShift(ExecutionContext.global)
    module = new Module(currentDb.xa, Blocker.liftExecutionContext(ExecutionContext.global))
  }

  "/list" should "create list" in {
    (for {
      userRes         <- request.signUpAndLogIn(userRequest)
      (_, token)      = userRes
      boardId         <- request.createAndGetBoard(token, boardRequest)
      cardList        = CardListRequest("name", closed = false, boardId, 0)
      postRequest     <- POST(cardList, Uri.unsafeFromString(s"/list"))
      postRequestAuth = postRequest.putHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, token.value)))
      postResponse    <- listRoutes.run(postRequestAuth)
    } yield {
      postResponse.status shouldEqual Ok
    }).unsafeRunSync
  }

  "/list" should "get list by listId" in {
    (for {
      userRes        <- request.signUpAndLogIn(userRequest)
      (_, token)     = userRes
      boardId        <- request.createAndGetBoard(token, boardRequest)
      cardListReq    = CardListRequest("name", closed = false, boardId, 0)
      listId         <- request.createAndGetCardList(token, cardListReq)
      getRequest     <- GET(Uri.unsafeFromString(s"/list/$listId"))
      getRequestAuth = getRequest.putHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, token.value)))
      getResponse    <- listRoutes.run(getRequestAuth)
      getCardList    <- getResponse.as[CardList]
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
      userRes        <- request.signUpAndLogIn(userRequest)
      (_, token)     = userRes
      boardId        <- request.createAndGetBoard(token, boardRequest)
      cardListReq    = CardListRequest("name", closed = false, boardId, 0)
      listId         <- request.createAndGetCardList(token, cardListReq)
      getRequest     <- DELETE(Uri.unsafeFromString(s"/list/$listId"))
      getRequestAuth = getRequest.putHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, token.value)))
      getResponse    <- listRoutes.run(getRequestAuth)
      getCardListId  <- getResponse.as[Long]
    } yield {
      getResponse.status shouldEqual Ok
      getCardListId shouldEqual listId
    }).unsafeRunSync
  }
}
