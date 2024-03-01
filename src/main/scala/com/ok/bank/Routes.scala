package com.ok.bank

import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import zio.Task
import zio.interop.catz._
object Routes {
  implicit def circeJsonDecoder[A](implicit
      decoder: io.circe.Decoder[A]
  ): EntityDecoder[Task, A] = jsonOf[Task, A]
  implicit def circeJsonEncoder[A](implicit
      encoder: io.circe.Encoder[A]
  ): EntityEncoder[Task, A] = jsonEncoderOf[Task, A]

  def accountRoutes(service: AccountService): HttpRoutes[Task] = {
    val dsl = new Http4sDsl[Task] {}
    import dsl._

    HttpRoutes.of[Task] { case GET -> Root / "account" / accountId =>
      service.getAccountDetails(accountId).flatMap {
        case Some(detail) =>
          Ok(detail) // Automatically encodes AccountDetail to JSON
        case None => NotFound()
      }
    }
  }

//  def transactionRoutes(service: AccountService): HttpRoutes[Task] = {
//    val dsl = new Http4sDsl[Task] {}
//    import dsl._
//
//    HttpRoutes.of[Task] { case req @ POST -> Root / "transaction" =>
//      req.decode[TransactionRequest] { transactionRequest =>
//        service.createTransaction(transactionRequest).flatMap {
//          case Right(transactionResponse) => Created(transactionResponse)
//          case Left("Account not found")  => NotFound("Account not found")
//          case Left("Insufficient funds") =>
//            UnprocessableEntity("Insufficient funds")
//          case Left(error) => BadRequest(error)
//        }
//      }
//    }
//  }

  def transactionRoutes(service: AccountService): HttpRoutes[Task] = {
    val dsl = new Http4sDsl[Task] {}
    import dsl._

    HttpRoutes.of[Task] {

      case req @ POST -> Root / "transaction" =>
        req.decode[TransactionRequest] { transactionRequest =>
          service.createTransaction(transactionRequest).flatMap {
            case Right(transactionResponse) =>
              Created(transactionResponse)
            case Left("Account not found") =>
              NotFound("Account not found")
            case Left("Insufficient funds") =>
              // Make sure to return UnprocessableEntity for insufficient funds
              UnprocessableEntity(
                "Insufficient funds after considering pending transactions"
              )
            case Left(
                  "Insufficient funds after considering pending transactions"
                ) =>
              UnprocessableEntity(
                "Insufficient funds after considering pending transactions"
              )
            case Left(error) =>
              BadRequest(error)
          }
        }
      // New route for GET /transaction/history/{accountId}
      case GET -> Root / "transaction" / "history" / accountId =>
        service.getTransactionHistory(accountId).flatMap {
          case Some(transactions) => Ok(transactions)
          case None               => NotFound("Account not found")
        }

    }
  }

  //  def transactionHistoryRoutes(service: AccountService): HttpRoutes[Task] = {
//    val dsl = new Http4sDsl[Task] {}
//    import dsl._
//
//    HttpRoutes.of[Task] {
//      case GET -> Root / "transaction" / "history" / accountId =>
//        service.getTransactionHistory(accountId).flatMap {
//          case Some(history) => Ok(history)
//          case None => NotFound("Account not found")
//        }
//    }
//  }

  def transactionHistoryRoutes(service: AccountService): HttpRoutes[Task] = {
    val dsl = new Http4sDsl[Task] {}
    import dsl._

    HttpRoutes.of[Task] {
      case GET -> Root / "transaction" / "history" / accountId =>
        service.getTransactionHistory(accountId).flatMap {
          case Some(history) => Ok(history)
          case None          => NotFound("Account not found")
        }
    }
  }

}


//
//package com.ok.bank
//
//import io.circe.generic.auto._
//import org.http4s._
//import org.http4s.circe._
//import org.http4s.dsl.Http4sDsl
//import zio.Task
//import zio.interop.catz._
//
///** Defines HTTP routes for account and transaction operations.
//  *
//  * @param service The account service interface for business logic.
//  */
//object Routes {
//  implicit def circeJsonDecoder[A](implicit
//      decoder: io.circe.Decoder[A]
//  ): EntityDecoder[Task, A] = jsonOf[Task, A]
//  implicit def circeJsonEncoder[A](implicit
//      encoder: io.circe.Encoder[A]
//  ): EntityEncoder[Task, A] = jsonEncoderOf[Task, A]
//
//  def apply(service: AccountService): HttpRoutes[Task] = {
//    val dsl = new Http4sDsl[Task] {}
//    import dsl._
//
//    HttpRoutes.of[Task] {
//      // Fetch account details
//      case GET -> Root / "account" / accountId =>
//        service.getAccountDetails(accountId).flatMap {
//          case Some(detail) => Ok(detail)
//          case None         => NotFound("Account not found")
//        }
//
//      // Create a new transaction
//      case req @ POST -> Root / "transaction" =>
//        req.decode[TransactionRequest] { transactionRequest =>
//          service.createTransaction(transactionRequest).flatMap {
//            case Right(transactionResponse) => Created(transactionResponse)
//            case Left(error) =>
//              error match {
//                case "Account not found" => NotFound("Account not found")
//                case "Insufficient funds" =>
//                  UnprocessableEntity(
//                    "Insufficient funds after considering pending transactions"
//                  )
//                case _ => BadRequest(error)
//              }
//          }
//        }
//
//      // Get transaction history for an account
//      case GET -> Root / "transaction" / "history" / accountId =>
//        service.getTransactionHistory(accountId).flatMap {
//          case Some(history) => Ok(history)
//          case None          => NotFound("Account not found")
//        }
//    }
//  }
//
//  /** Validates incoming requests to ensure they meet our expected format.
//    * This method should be expanded to include all necessary validation logic.
//    *
//    * @param transactionRequest The transaction request to validate.
//    * @return Either a string indicating an error or a validated transaction request.
//    */
//  private def validateTransactionRequest(
//      transactionRequest: TransactionRequest
//  ): Either[String, TransactionRequest] = {
//    // Example validation: ensure amount is positive
//    if (transactionRequest.amount.compareTo(BigDecimal(0)) <= 0)
//      Left("Amount must be positive")
//    else Right(transactionRequest)
//  }
//}
