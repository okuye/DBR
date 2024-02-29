package com.ok.bank

import zio.Task

import scala.util.control.NonFatal

class AccountServiceImpl extends AccountService {
  override def getAccountDetails(
      accountId: String
  ): Task[Option[AccountDetail]] =
    Task
      .effect {
        AccountDatabase.mockAccountsDb.get(accountId)
      }
      .catchSome { case NonFatal(e) =>
        Task.fail(
          e
        )
      }

  override def createTransaction(
      request: TransactionRequest
  ): Task[Either[String, TransactionResponse]] =
    Task.effect {
      AccountDatabase.mockAccountsDb.get(request.accountId) match {
        case Some(account) if account.balance >= request.amount =>
          Right(
            TransactionResponse(
              "txn-12345",
              request.accountId,
              request.amount,
              "COMPLETED",
              request.description
            )
          )
        case Some(_) =>
          Left("Insufficient funds")
        case None =>
          Left("Account not found")
      }
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
