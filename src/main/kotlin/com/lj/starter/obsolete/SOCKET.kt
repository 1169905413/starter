package com.lj.starter.obsolete


import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.core.net.NetClient
import io.vertx.core.net.NetServer
import io.vertx.core.net.NetSocket
import java.util.logging.Logger

fun main() {
  Vertx.vertx().deployVerticle(SOCKET)
}
object SOCKET : AbstractVerticle(){
  val logger = Logger.getLogger(this.toString())
  var deployID:String? = null
  var client : NetClient? = null
  var cs: NetSocket? = null
  var cs_server: NetSocket? = null
  var server: NetServer? = null


  override fun start() {
    val clt = vertx.createNetClient()
    val svr = vertx.createNetServer()


    clt.connect(502,"192.168.2.220") { cr ->
      println("client connected")
      val skt = cr.result()
//      println(skt)
      skt.handler { buffer ->
        println("client receive:" + buffer.length())
        println(String(buffer.bytes))
//        it.write(buffer)
      }

      skt.write("#010000")
    }




//    svr.connectHandler(){
//      println("server connections")
//
//      clt.connect(502,"192.168.2.220"){cr->
//        println("client connected")
//        val skt = cr.result()
//
//        it.handler { buffer->
//          println("server receive:" + buffer.length())
//          println(String(buffer.bytes))
//          skt.write(buffer)
//        }
//
//        skt.handler { buffer->
//          println("client receive:" + buffer.length())
//          println(String(buffer.bytes))
//
//          it.write(buffer)
//        }
//      }
//
//    }

//    svr.listen(502,"192.168.2.100"){
//      println(it.succeeded())
//    }



//    listen()
//    vertx.setTimer(1000){
//      connect()
//    }
  }

//  fun listen(port:Int = 50001) {
//    server!!.connectHandler{
//        socket->
//        cs_server = socket
//        println(cs_server)
//
////       socket.pipeTo(cs!!)
////        socket.handler { buffer ->
////          val bytes = buffer.bytes
////          println("server " + String(bytes))
////          if(cs != null)
////            cs!!.write(buffer)
////        }
//    }.listen(port){
//      println(it.result())
//    }
//  }
//
//  fun socketHandler(socket:NetSocket){
//
//  }
//
//
//  fun connect(ip:String = "172.1.1.101", port:Int = 50000) {
//    client!!.connect(port, ip) { res ->
//      if (res.succeeded()) {
//        logger.info("TCP Connected!")
//        var socket = res.result()
//        cs = socket
//        cs!!.handler { buffer ->
//          val bytes = buffer.bytes
//          println("client " + String(bytes))
//          if(cs_server != null){
//            cs_server!!.write(buffer)
//            println("write to server")
//          }else{
//            println("cs server is null")
//          }
//        }
//      }
//      else {
//        logger.severe("Failed to connect: ${res.cause()}")
//      }
//    }
//  }

}
