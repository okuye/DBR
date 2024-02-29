package com.ok.bank

import org.http4s._
import org.http4s.implicits._
import zio._
import zio.interop.catz._
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestEnvironment

object RoutesTest extends DefaultRunnableSpec {
  import io.circe.generic.auto._
  import org.http4s.circe.jsonOf

  implicit val accountDetailDecoder: EntityDecoder[Task, AccountDetail] =
    jsonOf[Task, AccountDetail]

  val testService = new AccountServiceImpl

  def spec: ZSpec[TestEnvironment, Any] = suite("RoutesTest")(
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
    }
  )
}
