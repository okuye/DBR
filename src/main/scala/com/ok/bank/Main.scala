package com.ok.bank

import cats.implicits._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import zio._
import zio.interop.catz._

object Main extends zio.App {

  val accountService = new AccountServiceImpl
  val routes =
    Routes.accountRoutes(accountService) <+> Routes.transactionRoutes(
      accountService
    ) <+>
      Routes.transactionHistoryRoutes(accountService)
  val httpApp = routes.orNotFound

  val server = ZIO.runtime[ZEnv].flatMap { implicit rts =>
    import cats.effect.Clock

    import java.util.concurrent.TimeUnit
    import scala.concurrent.duration.FiniteDuration

    implicit val timer: cats.effect.Timer[Task] = new cats.effect.Timer[Task] {
      override def clock: Clock[Task] = new Clock[Task] {
        override def realTime(unit: TimeUnit): Task[Long] =
          Task
            .effectTotal(System.currentTimeMillis())
            .map(unit.convert(_, TimeUnit.MILLISECONDS))

        override def monotonic(unit: TimeUnit): Task[Long] =
          Task
            .effectTotal(System.nanoTime())
            .map(unit.convert(_, TimeUnit.NANOSECONDS))
      }

      // FIXME : manually implementing Timer[Task] in this way bypasses some of the benefits of ZIO's environment, and it
      //  might indicate a deeper issue with dependency versions or project setup.
      override def sleep(duration: FiniteDuration): Task[Unit] =
        zio.clock
          .sleep(zio.duration.Duration.fromScala(duration))
          .provideLayer(zio.clock.Clock.live)

    }

    BlazeServerBuilder[Task](rts.platform.executor.asEC)
      .bindHttp(8080, "localhost")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.success)
  }

  override def run(args: List[String]): URIO[ZEnv, ExitCode] =
    server.exitCode
}
