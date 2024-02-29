package com.ok.bank

import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import zio.Task
import zio.interop.catz._

object Routes {

  // Place implicit conversions inside the object
  implicit def circeJsonDecoder[A](implicit decoder: io.circe.Decoder[A]): EntityDecoder[Task, A] = jsonOf[Task, A]
  implicit def circeJsonEncoder[A](implicit encoder: io.circe.Encoder[A]): EntityEncoder[Task, A] = jsonEncoderOf[Task, A]

  def accountRoutes(service: AccountService): HttpRoutes[Task] = {
    val dsl = new Http4sDsl[Task] {}
    import dsl._

    HttpRoutes.of[Task] {
      case GET -> Root / "account" / accountId =>
        service.getAccountDetails(accountId).flatMap {
          case Some(detail) => Ok(detail) // Automatically encodes AccountDetail to JSON
          case None         => NotFound()
        }
    }
  }
}
