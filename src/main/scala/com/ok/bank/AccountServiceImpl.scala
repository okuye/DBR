package com.ok.bank

import zio.Task

import scala.util.control.NonFatal

class AccountServiceImpl extends AccountService {
  override def getAccountDetails(accountId: String): Task[Option[AccountDetail]] =
    Task.effect {
      // Placeholder for actual database call
      // For demonstration, still using the mock database
      AccountDatabase.mockAccountsDb.get(accountId)
    }.catchSome {
      case NonFatal(e) =>
        // Log the error, return None, or handle the error as required
        Task.fail(e) // Re-throwing the exception here, but you might choose to handle it differently
    }
}

// Encapsulating the mock database within an object
object AccountDatabase {
  val mockAccountsDb: Map[String, AccountDetail] = Map(
    "123" -> AccountDetail(
      "123",
      BigDecimal(1000),
      UserDetail("Jane Doe", "jane.doe@example.com")
    ),
    "456" -> AccountDetail(
      "456",
      BigDecimal(500),
      UserDetail("John Smith", "john.smith@example.com")
    )
  )
}
