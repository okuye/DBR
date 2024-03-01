package com.ok.bank

import zio.Task

import java.time.LocalDateTime
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
  ): Task[Either[String, TransactionResponse]] = {
    for {
      accountOpt <- Task.effect(
        AccountDatabase.mockAccountsDb.get(request.accountId)
      )
      historyOpt <- Task.effect(
        AccountDatabase.mockTransactionHistoryDb
          .getOrElse(request.accountId, List.empty)
      )

      // Sort the pending transactions by timestamp in ascending order to apply them in the correct sequence
      sortedPendingTransactions = historyOpt
        .filter(_.status == "PENDING")
        .sortBy(_.timestamp)

      // Calculate the balance after considering pending transactions
      balanceAfterPending = accountOpt.map { account =>
        sortedPendingTransactions.foldLeft(account.balance) {
          (currentBalance, transaction) =>
            currentBalance - transaction.amount
        }
      }

      // Decide on the transaction's outcome based on the balance after considering pending transactions
      result <- balanceAfterPending match {
        case Some(balance) if balance >= request.amount =>
          Task.succeed(
            Right(
              TransactionResponse(
                "txn-12345",
                request.accountId,
                request.amount,
                "COMPLETED",
                request.description
              )
            )
          )
        case Some(_) =>
          Task.succeed(
            Left("Insufficient funds after considering pending transactions")
          )
        case None =>
          Task.succeed(Left("Account not found"))
      }
    } yield result
  }

  override def getTransactionHistory(
      accountId: String
  ): Task[Option[List[Transaction]]] = Task.effect {
    AccountDatabase.mockTransactionHistoryDb
      .get(accountId)
      .map(_.sortBy(_.timestamp)(Ordering[LocalDateTime].reverse))
  }
}

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

  // Mock transaction history data
  val mockTransactionHistoryDb: Map[String, List[Transaction]] = Map(
    "123" -> List(
      Transaction(
        "txn-001",
        "123",
        BigDecimal(-100),
        "Grocery shopping",
        "COMPLETED",
        LocalDateTime.now.minusDays(1)
      ),
      Transaction(
        "txn-002",
        "123",
        BigDecimal(-50),
        "Online Subscription",
        "COMPLETED",
        LocalDateTime.now.minusDays(2)
      ),
      Transaction(
        "txn-003",
        "123",
        BigDecimal(300),
        "Salary",
        "COMPLETED",
        LocalDateTime.now.minusDays(10)
      )
    ),
    "456" -> List(
      Transaction(
        "txn-004",
        "456",
        BigDecimal(-200),
        "Electronics Purchase",
        "COMPLETED",
        LocalDateTime.now.minusDays(3)
      ),
      Transaction(
        "txn-005",
        "456",
        BigDecimal(-150),
        "Utility Bill",
        "COMPLETED",
        LocalDateTime.now.minusDays(5)
      )
    )
  )
}
