package top.min0ri.akka
package utils

import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration.DurationInt

case class ServerConfig(addr: String, port: Int, timeout: Timeout, clerks: Int)

object ServerConfig {
  def parse: ServerConfig = {
    val config = ConfigFactory.load()
    val addr = config.getString("server.addr")
    val port = config.getInt("server.port")
    val timeout = config.getInt("server.timeout").seconds
    val clerks = config.getInt("server.office.clerks")
    ServerConfig(addr, port, Timeout(timeout), clerks)
  }
}
