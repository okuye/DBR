package com.ok.bank

import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._

object Main extends App {

  val accountService = new AccountServiceImpl
  val routes = Routes.accountRoutes(accountService).orNotFound

  val server = ZIO.runtime[ZEnv].flatMap { implicit rts =>
    BlazeServerBuilder[Task](rts.platform.executor.asEC)
      .bindHttp(8080, "localhost")
      .withHttpApp(routes)
      .serve
      .compile
      .drain
      .as(ExitCode.success)
  }

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    server.exitCode
}
