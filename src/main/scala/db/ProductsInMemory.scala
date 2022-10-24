package top.min0ri.akka
package db

import scala.collection.concurrent.{Map, TrieMap}

object ProductsInMemory extends Products {
  val products: Map[String, Product] = TrieMap[String, Product]()

  def getProducts(pages: Int, count: Int = 10): List[Product] = {
    products.values.toList.slice(pages * count, pages * count + count)
  }

  def getProduct(id: String): Option[Product] = {
    products.get(id)
  }

  def addProduct(product: Product): Unit = {
    products += (product.id -> product)
  }

  def addProducts(products: List[Product]): Unit = {
    products.foreach(addProduct)
  }

  def updateProduct(product: Product): Unit = {
    products += (product.id -> product)
  }

  def deleteProduct(id: String): Unit = {
    products -= id
  }

  def deleteAllProducts(): Unit = {
    products.clear()
  }
}
