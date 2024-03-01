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

//  override def createTransaction(
//      request: TransactionRequest
//  ): Task[Either[String, TransactionResponse]] =
//    Task.effect {
//      AccountDatabase.mockAccountsDb.get(request.accountId) match {
//        case Some(account) if account.balance >= request.amount =>
//          Right(
//            TransactionResponse(
//              "txn-12345",
//              request.accountId,
//              request.amount,
//              "COMPLETED",
//              request.description
//            )
//          )
//        case Some(_) =>
//          Left("Insufficient funds")
//        case None =>
//          Left("Account not found")
//      }
//    }

//  override def createTransaction(
//      request: TransactionRequest
//  ): Task[Either[String, TransactionResponse]] =
//    for {
//      accountOpt <- Task.effect(
//        AccountDatabase.mockAccountsDb.get(request.accountId)
//      )
//      historyOpt <- Task.effect(
//        AccountDatabase.mockTransactionHistoryDb.get(request.accountId)
//      )
//      balanceAfterPending = accountOpt.map { account =>
//        historyOpt
//          .getOrElse(Nil)
//          .filter(_.status == "PENDING")
//          .foldLeft(account.balance) { case (balance, transaction) =>
//            balance - transaction.amount
//          }
//      }
//      result <- balanceAfterPending match {
//        case Some(balance) if balance >= request.amount =>
//          Task.succeed(
//            Right(
//              TransactionResponse(
//                "txn-12345",
//                request.accountId,
//                request.amount,
//                "COMPLETED",
//                request.description
//              )
//            )
//          )
//        case Some(_) =>
//          Task.succeed(
//            Left("Insufficient funds after considering pending transactions")
//          )
//        case None => Task.succeed(Left("Account not found"))
//      }
//    } yield result


  override def createTransaction(request: TransactionRequest): Task[Either[String, TransactionResponse]] = {
    for {
      accountOpt <- Task.effect(AccountDatabase.mockAccountsDb.get(request.accountId))
      historyOpt <- Task.effect(AccountDatabase.mockTransactionHistoryDb.getOrElse(request.accountId, List.empty))
      balanceAfterPending = accountOpt.map { account =>
        // Calculate the balance after accounting for the amount of pending transactions
        historyOpt.filter(_.status == "PENDING").foldLeft(account.balance)(_ - _.amount)
      }
      result <- balanceAfterPending match {
        case Some(balance) if balance >= request.amount =>
          // If there are sufficient funds after considering pending transactions, process the transaction
          Task.succeed(Right(TransactionResponse("txn-12345", request.accountId, request.amount, "COMPLETED", request.description)))
        case Some(_) =>
          // If there are insufficient funds, return an error indicating insufficient funds
          Task.succeed(Left("Insufficient funds after considering pending transactions"))
        case None =>
          // If the account cannot be found, return an error indicating the account was not found
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

//  override def getTransactionHistory(
//      accountId: String
//  ): Task[Option[List[Transaction]]] = Task.effect {
//    AccountDatabase.mockTransactionHistoryDb.get(accountId)
//  }
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
