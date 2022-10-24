package top.min0ri.akka
package db

trait Products {
  def getProducts(pages: Int, count: Int = 10): List[Product]

  def getProduct(id: String): Option[Product]

  def addProduct(product: Product): Unit

  def addProducts(products: List[Product]): Unit

  def updateProduct(product: Product): Unit

  def deleteProduct(id: String): Unit

  def deleteAllProducts(): Unit
}

object Products {
  def get: Products = ProductsInMemory
}
