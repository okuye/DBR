// Models.scala
package com.ok.bank

case class UserDetail(name: String, email: String)
case class AccountDetail(
    accountId: String,
    balance: BigDecimal,
    userDetail: UserDetail
)

case class TransactionRequest(
    accountId: String,
    amount: BigDecimal,
    description: String
)
case class TransactionResponse(
    transactionId: String,
    accountId: String,
    amount: BigDecimal,
    status: String,
    description: String
)
