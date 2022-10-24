package top.min0ri.akka
package service

import akka.actor.typed.Behavior
import db.Products

import akka.actor.typed.receptionist.Receptionist.Register
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.scaladsl.Behaviors

object Clerk {

  import utils.MessageProtocols.Clerk._
  import utils.MessageProtocols.ClerkRequest

  val ClerkKey: ServiceKey[ClerkRequest] = ServiceKey[ClerkRequest]("clerk")
  val db: Products = Products.get

  def apply(): Behavior[ClerkRequest] = Behaviors.setup { context =>
    context.system.receptionist ! Register(ClerkKey, context.self)

    Behaviors.receive {
      case (context, BuyProduct(id, replyTo)) =>
        db.getProduct(id) match {
          case Some(product) =>
            context.log.info(s"Product ${product.name} is sold.")
            replyTo ! ProductFound(product)
            Behaviors.same
          case None =>
            context.log.info(s"Product $id is not found.")
            replyTo ! ProductNotFound(id)
            Behaviors.same
        }
      case (context, AddProduct(product, replyTo)) =>
        db.addProduct(product)
        context.log.info(s"Product ${product.name} is added.")
        replyTo ! OperationSuccess()
        Behaviors.same
      case (context, RemoveProduct(id, replyTo)) =>
        db.deleteProduct(id)
        context.log.info(s"Product $id is removed.")
        replyTo ! OperationSuccess()
        Behaviors.same
      case (context, FindProduct(id, service)) =>
        db.getProduct(id) match {
          case Some(product) =>
            context.log.info(s"Product ${product.name} is found.")
            service ! ProductFound(product)
            Behaviors.same
          case None =>
            context.log.info(s"Product $id is not found.")
            service ! ProductNotFound(id)
            Behaviors.same
        }
    }
  }
}
