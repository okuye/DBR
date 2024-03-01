package com.ok.bank

import cats.implicits.toSemigroupKOps
import com.ok.bank.Main.routes
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.circe.{jsonEncoder, jsonOf}
import org.http4s.implicits._
import zio._
import zio.interop.catz._
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment

object RoutesTest extends DefaultRunnableSpec {

  implicit val accountDetailDecoder: EntityDecoder[Task, AccountDetail] =
    jsonOf[Task, AccountDetail]
  implicit val transactionResponseDecoder
      : EntityDecoder[Task, TransactionResponse] =
    jsonOf[Task, TransactionResponse]

  val testService = new AccountServiceImpl

  // Define routes combining account and transaction routes
  val combinedRoutes = Routes.accountRoutes(testService).orNotFound <+> Routes
    .transactionRoutes(testService)
    .orNotFound

  def spec: ZSpec[TestEnvironment, Any] = suite("RoutesTest")(
    // Tests for POST /transaction
    testM("POST /transaction for an existing account with sufficient funds") {
      val transactionRequest =
        TransactionRequest("123", 100.0, "Test Transaction")
      val request = Request[Task](Method.POST, uri"/transaction").withEntity(
        transactionRequest.asJson
      )
      val responseTask =
        routes.run(request).value.map(_.getOrElse(Response.notFound))

      for {
        response <- responseTask
        status = response.status
        transactionResponse <- response.as[TransactionResponse]
      } yield assert(status)(equalTo(Status.Created)) &&
        assert(transactionResponse.status)(
          equalTo("COMPLETED")
        )
    },
    testM("POST /transaction for a non-existing account") {
      val transactionRequest =
        TransactionRequest("non-existing", 100.0, "Test Transaction")
      val request = Request[Task](Method.POST, uri"/transaction").withEntity(
        transactionRequest.asJson
      )
      // Convert OptionT[Task, Response[Task]] to Task[Response[Task]] by providing a default response for the None case
      val responseTask: Task[Response[Task]] =
        routes.run(request).value.map(_.getOrElse(Response.notFound))

      // Extract the status from the response
      val statusTask: Task[Status] = responseTask.map(_.status)

      // Use assertM to check the status
      assertM(statusTask)(equalTo(Status.NotFound))
    },
    testM("POST /transaction with insufficient funds") {
      val transactionRequest =
        TransactionRequest("123", 10000.0, "Large Transaction")
      val request = Request[Task](Method.POST, uri"/transaction").withEntity(
        transactionRequest.asJson
      )
      val responseTask =
        routes.run(request).value.map(_.getOrElse(Response.notFound))

      for {
        response <- responseTask
        status = response.status
      } yield assert(status)(equalTo(Status.UnprocessableEntity))
    },
    testM("GET /transaction/history/123 returns transaction history") {
      val request = Request[Task](Method.GET, uri"/transaction/history/123")
      // Convert OptionT[Task, Response[Task]] to Task[Response[Task]] by providing a default response for the None case
      val responseTask: Task[Response[Task]] =
        routes.run(request).value.map(_.getOrElse(Response.notFound))

      // Extract the status from the response
      val statusTask: Task[Status] = responseTask.map(_.status)

      // Use assertM to check the status, ensuring that we are working with a Task[Status]
      assertM(statusTask)(equalTo(Status.Ok))
    },
    testM("GET /transaction/history/non-existing returns 404") {
      val request =
        Request[Task](Method.GET, uri"/transaction/history/non-existing")
      // Convert OptionT[Task, Response[Task]] to Task[Response[Task]] by providing a default response for the None case
      val responseTask: Task[Response[Task]] =
        routes.run(request).value.map(_.getOrElse(Response.notFound))

      // Extract the status from the response
      val statusTask: Task[Status] = responseTask.map(_.status)

      // Use assertM to check the status, ensuring that we are working with a Task[Status]
      assertM(statusTask)(equalTo(Status.NotFound))
    },
    testM("GET /account/123 returns 200 and account details") {
      val request = Request[Task](Method.GET, uri"/account/123")
      val response = Routes.accountRoutes(testService).orNotFound.run(request)

      assertM(response.map(_.status))(equalTo(Status.Ok)) *>
        assertM(response.flatMap(r => r.as[AccountDetail]))(
          equalTo(
            AccountDetail(
              "123",
              BigDecimal(1000),
              UserDetail("Jane Doe", "jane.doe@example.com")
            )
          )
        )
    },
    testM("GET /account/non-existing returns 404") {
      val request = Request[Task](Method.GET, uri"/account/non-existing")
      val response =
        Routes.accountRoutes(testService).orNotFound.run(request).map(_.status)

      assertM(response)(equalTo(Status.NotFound))
    },
    testM(
      "GET /transaction/history/123 returns transaction history for existing account"
    ) {
      val request = Request[Task](Method.GET, uri"/transaction/history/123")
      val response =
        routes.run(request).value.map(_.getOrElse(Response.notFound))

      for {
        response <- response
        status = response.status
        transactions <- response.as[List[
          Transaction
        ]] // Assuming you have an appropriate decoder for List[Transaction]
      } yield assert(status)(equalTo(Status.Ok)) &&
        assert(transactions.nonEmpty)(isTrue)
    },
    testM(
      "GET /transaction/history/non-existing returns 404 for non-existing account"
    ) {
      val request =
        Request[Task](Method.GET, uri"/transaction/history/non-existing")
      val response =
        routes.run(request).value.map(_.getOrElse(Response.notFound))

      assertM(response.map(_.status))(equalTo(Status.NotFound))
    }
  )
}
