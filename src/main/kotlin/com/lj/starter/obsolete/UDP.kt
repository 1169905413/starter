package com.lj.starter.obsolete



import com.lj.starter.Modbus
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.datagram.DatagramSocket
import java.util.logging.Logger

fun main() {
  Vertx.vertx().deployVerticle(UDP)
}

object UDP :  AbstractVerticle()  {
  private val logger: Logger = Logger.getLogger(this.toString())
  var udp: DatagramSocket? = null




  override fun start(startPromise: Promise<Void>?) {
    logger.info("Deploy UDP!")
    udp = vertx.createDatagramSocket()
    print(udp)
//    listen()
    udp!!.send("#010001\r", 1025, "192.168.2.220") { asyncResult ->
      logger.fine("Send succeeded? ${asyncResult.succeeded()}")
    }
//    udp!!.send("#010001", 502, "192.168.2.220") { asyncResult ->
//      logger.fine("Send succeeded? ${asyncResult.succeeded()}")
//    }
  }

//  fun listen (ip:String = "192.168.2.220", port:Int = 502) {
//    udp!!.listen(port, ip) { asyncResult ->
//      if (asyncResult.succeeded()) {
//        logger.info("udp listened")
//        udp!!.handler { packet ->
//          logger.fine("UDP:${System.currentTimeMillis()}")
//          val data = packet.data().bytes
//          println(data)
//        }
//      } else {
//        logger.severe("Listen failed${asyncResult.cause()}")
//      }
//    }
//  }
  fun pause() {
    udp!!.pause()
  }

  val getdata:(ByteArray,Int,Int) ->Double ={ x,y,z->Modbus.valueudp(x,y,z).toDouble()}

//  fun resume() {
//    DEVICE.startpoint = 0
//    udp!!.resume()
//  }
//  fun resume() {
//    DEVICE.startpoint = 0
//    udp!!.resume()
//  }

  fun receive() {
    val data = byteArrayOf()
    var buffer = Buffer.buffer(data)
    udp!!.send(buffer, 502, "192.168.1.30") { asyncResult ->
      logger.fine("Send succeeded? ${asyncResult.succeeded()}")
    }
  }

  fun close() {
    udp!!.close()
    udp = vertx.createDatagramSocket()
  }

}
