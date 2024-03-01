package com.ok.bank

import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import zio.interop.catz._
import zio.{Task, ZIO}
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

  def transactionRoutes(service: AccountService): HttpRoutes[Task] = {
    val dsl = new Http4sDsl[Task] {}
    import dsl._

    HttpRoutes.of[Task] {

      case req @ POST -> Root / "transaction" =>
        req.decode[TransactionRequest] { transactionRequest =>
          service
            .createTransaction(transactionRequest)
            .flatMap {
              case Right(transactionResponse) =>
                Created(transactionResponse)
              case Left("Account not found") =>
                NotFound("Account not found")
              case Left(
                    "Insufficient funds after considering pending transactions"
                  ) =>
                UnprocessableEntity(
                  "Insufficient funds after considering pending transactions"
                )
              case Left(error) =>
                InternalServerError(s"An unexpected error occurred: $error")
            }
            .catchAll { error =>
              // Log the error and return a generic error response
              ZIO.effectTotal(println(s"Error processing request: $error")) *>
                InternalServerError("An unexpected error occurred")
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

  def healthCheckRoutes: HttpRoutes[Task] = {
    val dsl = new Http4sDsl[Task] {}
    import dsl._

    HttpRoutes.of[Task] { case GET -> Root / "health" =>
      Ok("Service is up")
    }
  }

  def errorHandling(routes: HttpRoutes[Task]): HttpRoutes[Task] = {
    HttpRoutes.of[Task] { case req @ _ =>
      routes
        .run(req)
        .getOrElseF(
          // Handle the case where no route matches
          ZIO.succeed(Response[Task](Status.NotFound))
        )
        .catchAll { cause =>
          // Log the error and return a generic error response
          ZIO
            .effectTotal(println(s"Unhandled error: ${cause.getMessage}"))
            .as(
              Response[Task](Status.InternalServerError)
                .withEntity("An unexpected error occurred")
            )
        }
    }
  }

}
