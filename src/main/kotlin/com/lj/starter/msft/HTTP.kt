package com.lj.starter.msft

import com.lj.starter.msft.CACHE
//import com.lj.starter.msft_can_ai.COM
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import java.util.logging.Logger


object HTTP : AbstractVerticle(){

  val logger = Logger.getLogger(this.toString())

  override fun start(startPromise: Promise<Void>) {
    logger.info("Deploy HTTP!")
    var server = vertx.createHttpServer()
    var router = Router.router(vertx)
//    val ba = BasicAuthHandler.create()
    router.route().handler(BodyHandler.create())

    router.get("/udp/listen").handler { routingContext ->
      UDP.listen("raw")
      UDP.listen("raw1")
      var response = routingContext.response()
      response.putHeader("content-type", "text/plain")
      response.end("UDP listen")
    }

    router.get("/udp/close").handler { routingContext ->
      UDP.close()
      var response = routingContext.response()
      response.putHeader("content-type", "text/plain")
      response.end("UDP close")
    }

    router.get("/udp/pause").handler { routingContext ->
      UDP.pause()
      var response = routingContext.response()
      response.putHeader("content-type", "text/plain")
      response.end("UDP pause")
    }

    router.get("/udp/resume").handler { routingContext ->
      UDP.resume()
      var response = routingContext.response()
      response.putHeader("content-type", "text/plain")
      response.end("UDP resume")
    }

    router.post("/com/open").handler { routingContext ->
      val body = routingContext.bodyAsJson
      COM.connect(body.getString("name"))
      var response = routingContext.response()
      response.putHeader("content-type", "text/plain")
      response.end("COM Open")
    }

    router.post("/com/command").handler { routingContext ->
//      logger.info(routingContext.body.toString())
      val body = routingContext.bodyAsJsonArray

      for (i in 0 until body.size()) {
        val obj = body.getJsonObject(i)
        logger.info(obj.toString())
        COM.execute2(obj.getString("id"), obj.getString("value"))
      }
      var response = routingContext.response()
      response.putHeader("content-type", "text/plain")
      response.end("COM run")
    }


    router.get("/com/close").handler { routingContext ->
      COM.close()
      var response = routingContext.response()
      response.putHeader("content-type", "text/plain")
      response.end("COM Close")
    }

    router.get("/com/status").handler { routingContext ->
      var response = routingContext.response()
      response.putHeader("content-type", "text/json")
      response.end(JsonObject().put("com",JsonObject(COM.result.toMap()).toString()).put("udp",DEVICE.status()).toString())
    }

    router.get("/com/ports").handler { routingContext ->
      var response = routingContext.response()
      response.putHeader("content-type", "text/json")
      response.end(JsonArray(COM.ports().toList()).toString())
    }

    router.get("/com/isopen").handler { routingContext ->
      var response = routingContext.response()
      response.putHeader("content-type", "text/plain")
      response.end(COM.isOpen().toString())
    }

    router.get("/udp/cache").handler { routingContext ->
      var response = routingContext.response()
      response.putHeader("content-type", "text/json")
      response.end(CACHE.fetch().toString())
    }


    router.post("/udp/offset").handler { routingContext ->
      var response = routingContext.response()
      val body = routingContext.bodyAsJson
      val id = body.getInteger("id")
      val value = body.getDouble("value")
      val auto = body.getBoolean("auto")

      response.putHeader("content-type", "text/json")
      response.end(DEVICE.resetoffset(id, value, auto).toString())
    }


    router.post("/udp/threshold").handler { routingContext ->
      var response = routingContext.response()
      val body = routingContext.bodyAsJson
      val id = body.getInteger("id")
      val value = body.getDouble("value")

      response.putHeader("content-type", "text/json")
      response.end(DEVICE.resetthreshold(id, value).toString())
    }


    router.post("/udp/pass").handler { routingContext ->
      var response = routingContext.response()
      val body = routingContext.bodyAsJson
      val id = body.getInteger("id")
      val value = body.getLong("value")

      response.putHeader("content-type", "text/json")
      response.end(DEVICE.addPass(id, value).toString())
    }

    router.post("/udp/cycle").handler { routingContext ->
      var response = routingContext.response()
      val body = routingContext.bodyAsJson
      val value = body.getLong("value")

      response.putHeader("content-type", "text/json")
      DEVICE.addCycle( value)
      response.end("OK")
    }

    router.post("/udp/slope").handler { routingContext ->
      var response = routingContext.response()
      val body = routingContext.bodyAsJson
      val id = body.getInteger("id")
      val value = body.getDouble("value")

      response.putHeader("content-type", "text/json")
      response.end(DEVICE.resetslope(id, value).toString())
    }

    router.get("/udp/autovalue").handler { routingContext ->
      var response = routingContext.response()
      DEVICE.setCompensationValue()
      response.putHeader("content-type", "text/json")
      response.end("autostart")
    }


    router.get("/udp/autostart").handler { routingContext ->
      var response = routingContext.response()
      DEVICE.setCompensation(true)
      response.putHeader("content-type", "text/json")
      response.end("autostart")
    }


    router.get("/udp/autostop").handler { routingContext ->
      var response = routingContext.response()
      DEVICE.setCompensation(false)
      response.putHeader("content-type", "text/json")
      response.end("autostop")
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
