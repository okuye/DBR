package com.ok.bank

import cats.effect.Timer
import cats.implicits.toSemigroupKOps
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import zio._
import zio.clock.Clock
import zio.interop.catz._

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
object Main extends zio.App {
  val accountService = new AccountServiceImpl
  val routes = Routes.accountRoutes(accountService) <+>
    Routes.transactionRoutes(accountService) <+>
    Routes.transactionHistoryRoutes(accountService)
  val httpApp = routes.orNotFound

  // Explicitly create a Timer[Task] instance
  implicit val timer: Timer[Task] = new Timer[Task] {
    override def clock: cats.effect.Clock[Task] = new cats.effect.Clock[Task] {
      override def realTime(unit: TimeUnit): Task[Long] =
        ZIO
          .effectTotal(System.currentTimeMillis())
          .map(unit.convert(_, TimeUnit.MILLISECONDS))

      override def monotonic(unit: TimeUnit): Task[Long] =
        ZIO
          .effectTotal(System.nanoTime())
          .map(unit.convert(_, TimeUnit.NANOSECONDS))
    }

    override def sleep(duration: FiniteDuration): Task[Unit] =
      ZIO
        .sleep(zio.duration.Duration.fromScala(duration))
        .provideLayer(Clock.live)
  }

  val server: ZIO[ZEnv, Throwable, Unit] =
    ZIO.runtime[ZEnv].flatMap { implicit rts =>
      val ec: ExecutionContext = rts.platform.executor.asEC
      BlazeServerBuilder[Task](ec)
        .bindHttp(8080, "localhost")
        .withHttpApp(httpApp)
        .resource
        .toManagedZIO
        .useForever
        .orDie
    }

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    server.exitCode
}

