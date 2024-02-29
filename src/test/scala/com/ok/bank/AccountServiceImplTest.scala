package com.ok.bank

import zio.test._
import zio.test.Assertion._
import zio.test.environment.TestEnvironment

object AccountServiceImplTest extends DefaultRunnableSpec {
  def spec: ZSpec[TestEnvironment, Any] = suite("AccountServiceImplTest")(
    testM("getAccountDetails returns details for existing account") {
      val service = new AccountServiceImpl
      for {
        details <- service.getAccountDetails("123")
      } yield assert(details)(
        isSome(
          equalTo(
            AccountDetail(
              "123",
              BigDecimal(1000),
              UserDetail("Jane Doe", "jane.doe@example.com")
            )
          )
        )
      )
    },
    testM("getAccountDetails returns None for non-existing account") {
      val service = new AccountServiceImpl
      for {
        details <- service.getAccountDetails("non-existing")
      } yield assert(details)(isNone)
    }
  )
}
