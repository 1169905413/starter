package com.lj.starter.lvdt

import com.lj.starter.msft.DEVICE
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import java.util.logging.Logger


object HTTP : AbstractVerticle(){

  val logger = Logger.getLogger(this.toString())

  override fun start(startPromise: Promise<Void>) {
    logger.info("Deploy HTTP!")
    val server = vertx.createHttpServer()
    val router = Router.router(vertx)
    var connected = false
    router.route().handler(BodyHandler.create())

    router.get("/tcp/start").handler { routingContext ->
      TCP.startTimer(1000)
      val response = routingContext.response()
      response.putHeader("content-type", "text/plain")
      response.end("TCP Start")
    }

    router.get("/tcp/stop").handler { routingContext ->
      TCP.stopTimer()
      val response = routingContext.response()
      response.putHeader("content-type", "text/plain")
      response.end("TCP stop")
    }

    router.get("/tcp/connect").handler { routingContext ->
      if(! connected ){
        TCP.connect()
        connected = true
      }
      val response = routingContext.response()
      response.putHeader("content-type", "text/plain")
      response.end("TCP Connect")
    }


    router.get("/tcp/status").handler { routingContext ->
      val response = routingContext.response()
      response.putHeader("content-type", "text/plain")
      response.end(TCP.failed.toString())
    }

    router.post("/tcp/slope").handler { routingContext ->
      var response = routingContext.response()
      val body = routingContext.bodyAsJson
      val id = body.getInteger("id")
      val value = body.getDouble("value")

      response.putHeader("content-type", "text/json")
      TCP.slope[id]=value
      response.end(TCP.slope.toString())
    }

    router.post("/tcp/offset").handler { routingContext ->
      var response = routingContext.response()
      val body = routingContext.bodyAsJson
      val id = body.getInteger("id")
      val value = body.getDouble("value")

      response.putHeader("content-type", "text/json")
      TCP.offset[id]=value
      response.end(TCP.offset.toString())
    }

    server.requestHandler(router).listen(8080) { res ->
      if (res.succeeded()) {
        logger.info("Server is now listening!")
      } else {
        logger.severe("Failed to bind!"+res.cause())
      }
    }
  }

}
