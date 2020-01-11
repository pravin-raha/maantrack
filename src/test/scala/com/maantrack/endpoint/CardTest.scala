package com.maantrack.endpoint

import java.time.Instant

import cats.effect.{ Blocker, ContextShift, IO }
import com.maantrack.Module
import com.maantrack.domain._
import com.maantrack.test.{ BaseTest, Requests, TestEmbeddedPostgres }
import dev.profunktor.auth.jwt.JwtToken
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.{ AuthScheme, Credentials, HttpApp, Uri }
import org.scalatest.concurrent.Eventually

import scala.concurrent.ExecutionContext

class CardTest extends BaseTest with TestEmbeddedPostgres with Eventually with Http4sClientDsl[IO] with Http4sDsl[IO] {

  implicit var contextShift: ContextShift[IO] = _
  private var module: Module[IO]              = _

  private lazy val cardRoute: HttpApp[IO] = module.httpApp
  private lazy val request                = new Requests(module)

  private val userRequest: UserRequest =
    UserRequest("test@test.com", "fname", "lname", Role.Customer, "test", "test", Instant.now)

  private val boardRequest: BoardRequest =
    BoardRequest("name", Some("description"), closed = false, pinned = false, "url", starred = false)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    contextShift = IO.contextShift(ExecutionContext.global)
    module = new Module(currentDb.xa, Blocker.liftExecutionContext(ExecutionContext.global))
  }

  "/card" should "create card" in {
    (for {
      userRes    <- request.signUpAndLogIn(userRequest)
      (_, token) = userRes
      boardId    <- request.createAndGetBoard(token, boardRequest)
      cardList   = CardListRequest("name", closed = false, boardId, 0)
      cardListId <- request.createAndGetCardList(token, cardList)
      cardRequest = CardRequest(
        closed = false,
        description = Some("description"),
        due = Instant.now(),
        dueCompleted = false,
        boardId = boardId,
        listId = cardListId,
        name = "name",
        pos = 0
      )
      postRequest     <- POST(cardRequest, Uri.unsafeFromString(s"/card"))
      postRequestAuth = postRequest.putHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, token.value)))
      postResponse    <- cardRoute.run(postRequestAuth)
    } yield {
      postResponse.status shouldEqual Ok
    }).unsafeRunSync
  }

  "/card" should "get card by cardId" in {
    (for {
      cardReq                   <- createCardAndGetId
      (token, cardId, cardList) = cardReq
      getRequest                <- GET(Uri.unsafeFromString(s"/card/$cardId"))
      getRequestAuth            = getRequest.putHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, token.value)))
      getResponse               <- cardRoute.run(getRequestAuth)
      getCard                   <- getResponse.as[Card]
    } yield {
      getResponse.status shouldEqual Ok
      getCard.name shouldEqual cardList.name
      getCard.closed shouldEqual cardList.closed
      getCard.boardId shouldEqual cardList.boardId
      getCard.pos shouldEqual cardList.pos
      getCard.listId shouldEqual cardList.listId
      getCard.dueCompleted shouldEqual cardList.dueCompleted
      getCard.description shouldEqual cardList.description
    }).unsafeRunSync
  }

  "/card" should "delete card by cardId" in {
    (for {
      cardReq            <- createCardAndGetId
      (token, cardId, _) = cardReq
      getRequest         <- DELETE(Uri.unsafeFromString(s"/card/$cardId"))
      getRequestAuth     = getRequest.putHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, token.value)))
      getResponse        <- cardRoute.run(getRequestAuth)
      getCardId          <- getResponse.as[Long]
    } yield {
      getResponse.status shouldEqual Ok
      getCardId shouldEqual cardId
    }).unsafeRunSync
  }

  private def createCardAndGetId: IO[(JwtToken, Long, CardRequest)] =
    for {
      userRes    <- request.signUpAndLogIn(userRequest)
      (_, token) = userRes
      boardId    <- request.createAndGetBoard(token, boardRequest)
      cardList   = CardListRequest("name", closed = false, boardId, 0)
      cardListId <- request.createAndGetCardList(token, cardList)
      cardReq = CardRequest(
        closed = false,
        description = Some("description"),
        due = Instant.now(),
        dueCompleted = false,
        boardId = boardId,
        listId = cardListId,
        name = "name",
        pos = 0
      )
      cardId <- request.createAndGetCard(token, cardReq)
    } yield (token, cardId, cardReq)

}
