package top.min0ri.akka
package service

import akka.NotUsed
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior, SupervisorStrategy, Terminated}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import utils.MessageProtocols.Clerk._
import utils.MessageProtocols.Office._
import utils.ServerConfig

object Store {
  def apply(config: ServerConfig): Behavior[NotUsed] = {
    Behaviors.supervise(store(config))
      .onFailure[Exception](SupervisorStrategy.restart)
  }

  def store(config: ServerConfig): Behavior[NotUsed] = {
    Behaviors.setup { context =>
      implicit val system: ActorSystem[Nothing] = context.system
      implicit val executionContext: ExecutionContextExecutor = context.executionContext
      implicit val timeout: Timeout = config.timeout

      val service = context.spawn(Office(), "store-service")
      (0 until config.clerks).foreach { id =>
        context.spawn(Clerk(), s"clerk-$id")
      }
      context.log.info(s"Store service started with ${config.clerks} clerks.")
      context.watch(service)

      val productRoute = pathPrefix("products" / Segment) { id =>
        post {
          val future = service ? (Order(id, _))
          onComplete(future) {
            case Success(ProductFound(product)) =>
              complete(StatusCodes.OK, s"Ordering product: $product")
            case Success(ProductNotFound(id)) =>
              complete(StatusCodes.BadRequest, s"Product $id is not found.")
            case Success(OperationSuccess()) => complete(StatusCodes.NoContent)
            case Failure(exception) =>
              complete(StatusCodes.InternalServerError, s"An error occurred: ${exception.getMessage}")
          }
        } ~ get {
          val future = service ? (ref => Query(id, ref))
          onComplete(future) {
            case Success(ProductFound(product)) =>
              complete(StatusCodes.OK, s"Product Info: $product")
            case Success(ProductNotFound(id)) =>
              complete(StatusCodes.BadRequest, s"Product $id is not found.")
            case Success(OperationSuccess()) => complete(StatusCodes.NoContent)
            case Failure(exception) =>
              complete(StatusCodes.InternalServerError, s"An error occurred: ${exception.getMessage}")
          }
        }
      }

      val adminRoute = pathPrefix("admin" / Segment) { command =>
        get {
          if (command == "stop") {
            service ! Close()
            complete(StatusCodes.OK, s"Store service stopped.")
          } else {
            complete(StatusCodes.BadRequest, s"Invalid command.")
          }
        }
      }

      val bindingFuture = Http()
        .newServerAt(config.addr, config.port)
        .bind(productRoute ~ adminRoute)

      context.log.info(s"Server is now open on http://${config.addr}:${config.port}")

      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          bindingFuture
            .flatMap(_.unbind())
            .onComplete(_ => system.terminate())
          Behaviors.stopped
      }
    }
  }
}
