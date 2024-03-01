// Models.scala
package com.ok.bank
import io.circe.{Decoder, Encoder}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

object Transaction {
  implicit val encodeLocalDateTime: Encoder[LocalDateTime] =
    Encoder.encodeLocalDateTimeWithFormatter(
      DateTimeFormatter.ISO_LOCAL_DATE_TIME
    )
  implicit val decodeLocalDateTime: Decoder[LocalDateTime] =
    Decoder.decodeLocalDateTimeWithFormatter(
      DateTimeFormatter.ISO_LOCAL_DATE_TIME
    )
}

case class Transaction(
    transactionId: String,
    accountId: String,
    amount: BigDecimal,
    description: String,
    status: String,
    timestamp: LocalDateTime
)
