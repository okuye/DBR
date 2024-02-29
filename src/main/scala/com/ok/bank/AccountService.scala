// AccountService.scala
package com.ok.bank

import zio.Task

trait AccountService {
  def getAccountDetails(accountId: String): Task[Option[AccountDetail]]
}
