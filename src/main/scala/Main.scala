package top.min0ri.akka

import akka.actor.typed.ActorSystem
import service.Store

import utils.ServerConfig

object Main extends App {
  val config = ServerConfig.parse
  ActorSystem(Store(config), "akka-store")
}
