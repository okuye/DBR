// AccountService.scala
package com.ok.bank

import zio.Task

trait AccountService {
  def getAccountDetails(accountId: String): Task[Option[AccountDetail]]
  def createTransaction(
      request: TransactionRequest
  ): Task[Either[String, TransactionResponse]]

  def getTransactionHistory(accountId: String): Task[Option[List[Transaction]]]
}
