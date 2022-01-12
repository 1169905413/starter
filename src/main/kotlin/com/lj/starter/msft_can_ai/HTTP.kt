package com.lj.starter.msft_can_ai


import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import java.util.logging.Logger
import kotlin.math.sin


object HTTP : AbstractVerticle(){

  val logger = Logger.getLogger(this.toString())

  override fun start(startPromise: Promise<Void>) {
    logger.info("Deploy HTTP!")
    var server = vertx.createHttpServer()
    var router = Router.router(vertx)
//    val ba = BasicAuthHandler.create()
    router.route().handler(BodyHandler.create())

    router.get("/udp/listen").handler { routingContext ->
//      UDP.listen()
      var response = routingContext.response()
      response.putHeader("content-type", "text/plain")
      response.end("UDP listen")
    }

    router.get("/udp/close").handler { routingContext ->
//      UDP.close()
      var response = routingContext.response()
      response.putHeader("content-type", "text/plain")
      response.end("UDP close")
    }

    router.get("/udp/pause").handler { routingContext ->
//      UDP.pause()
      var response = routingContext.response()
      response.putHeader("content-type", "text/plain")
      response.end("UDP pause")
    }

    router.get("/udp/resume").handler { routingContext ->
//      UDP.resume()
      var response = routingContext.response()
      response.putHeader("content-type", "text/plain")
      response.end("UDP resume")
    }

    router.post("/com/open").handler { routingContext ->
      val body = routingContext.bodyAsJson
//      COM.connect(body.getString("name"))
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
        DEVICE.update(obj.getString("value").trim())
        (CAN2.motor.address[0] as CopleyMotor).translate( obj.getString("value"))
      }

      var response = routingContext.response()
      response.putHeader("content-type", "text/plain")
      response.end("COM run")
    }


    router.get("/com/close").handler { routingContext ->
//      COM.close()
      var response = routingContext.response()
      response.putHeader("content-type", "text/plain")
      response.end("COM Close")
    }

    router.get("/com/status").handler { routingContext ->
      var response = routingContext.response()
      response.putHeader("content-type", "text/json")
      val motor = CAN2.motor.address[0] as CopleyMotor
      response.end(JsonObject().put("com",JsonObject(motor.result.toMap()).toString()).put("udp",DEVICE.status()).put("motor",motor.toJson()).toString())
    }

    router.get("/com/ports").handler { routingContext ->
      var response = routingContext.response()
      response.putHeader("content-type", "text/json")
      response.end(JsonArray().toString())
    }

    router.get("/com/isopen").handler { routingContext ->
      var response = routingContext.response()
      response.putHeader("content-type", "text/plain")
      response.end("false")
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



//    val list = (0..1023).map{  (sin((it.toDouble()/10).toDouble()) )}

    router.get("/can/motor").handler { routingContext ->
      var response = routingContext.response()
      DEVICE.setCompensation(true)
      response.putHeader("content-type", "text/json")
      response.end(CAN2.addresses[0].motorData().toString())
    }

    router.post("/msft/motor").handler{
      val body = it.bodyAsJson
      val values = body.getJsonArray("values")
      val chan = body.getInteger("channel")
      val motor = CAN2.motor.address[chan] as CopleyMotor
      motor.update(values)


//      STATUS.stations[chan].update(values)
      val response = it.response()
      response.putHeader("content-type", "text/plain")
      response.end("OK")
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
