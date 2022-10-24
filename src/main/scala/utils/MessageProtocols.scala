package top.min0ri.akka
package utils

import akka.actor.typed.ActorRef

import db.Product

object MessageProtocols {

  sealed trait ClerkResponse

  sealed trait ClerkRequest {
    val service: ActorRef[ClerkResponse]
  }

  case object Clerk {
    case class AddProduct(products: Product, service: ActorRef[ClerkResponse]) extends ClerkRequest

    case class BuyProduct(id: String, service: ActorRef[ClerkResponse]) extends ClerkRequest

    case class RemoveProduct(id: String, service: ActorRef[ClerkResponse]) extends ClerkRequest

    case class FindProduct(id: String, service: ActorRef[ClerkResponse]) extends ClerkRequest

    case class OperationSuccess() extends ClerkResponse

    case class ProductNotFound(id: String) extends ClerkResponse

    case class ProductFound(product: Product) extends ClerkResponse
  }

  sealed trait OfficeRequest

  case object Office {
    case class Order(id: String, replyTo: ActorRef[ClerkResponse]) extends OfficeRequest

    case class Query(id: String, replyTo: ActorRef[ClerkResponse]) extends OfficeRequest

    case class Remove(id: String, replyTo: ActorRef[ClerkResponse]) extends OfficeRequest

    case class Restock(products: List[Product], replyTo: ActorRef[ClerkResponse]) extends OfficeRequest

    case class Close() extends OfficeRequest
  }
}
