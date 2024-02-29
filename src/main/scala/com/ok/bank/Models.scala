// Models.scala
package com.ok.bank

case class UserDetail(name: String, email: String)
case class AccountDetail(
    accountId: String,
    balance: BigDecimal,
    userDetail: UserDetail
)
