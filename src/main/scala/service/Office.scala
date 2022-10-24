package top.min0ri.akka
package service

import akka.actor.typed.{Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.{Behaviors, Routers}

import utils.MessageProtocols.Office._
import utils.MessageProtocols.OfficeRequest
import utils.MessageProtocols.Clerk._

object Office {
  def apply(): Behavior[OfficeRequest] = {
    Behaviors.setup { context =>
      val group = Routers.group(Clerk.ClerkKey)
      val router = context.spawn(group, "clerks-group")

      Behaviors.receiveMessage {
        case Order(id, replyTo) =>
          router ! BuyProduct(id, replyTo)
          Behaviors.same
        case Query(id, replyTo) =>
          router ! FindProduct(id, replyTo)
          Behaviors.same
        case Remove(id, replyTo) =>
          router ! RemoveProduct(id, replyTo)
          Behaviors.same
        case Restock(products, replyTo) =>
          products.foreach(router ! AddProduct(_, replyTo))
          Behaviors.same
        case Close() =>
          Behaviors.stopped
      }
    }
  }
}
