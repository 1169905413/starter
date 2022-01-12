package com.lj.starter.stdt


import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.core.json.get
import java.util.logging.Logger


object HTTP : AbstractVerticle(){

  val logger = Logger.getLogger(this.toString())
  override fun start(startPromise: Promise<Void>) {
    logger.info("Deploy HTTP!")
    val server = vertx.createHttpServer()
    val router = Router.router(vertx)
    router.route().handler(BodyHandler.create())


    router.post("/stdt/station").handler{
      val body = it.bodyAsJson
      val values = body.getJsonArray("values")
      val chan = body.getInteger("channel")
      STATUS.stations[chan].update(values)
      val response = it.response()
      response.putHeader("content-type", "text/plain")
      response.end("OK")
    }


    router.post("/com/command").handler { routingContext ->
//      logger.info(routingContext.body.toString())
      val body = routingContext.bodyAsJsonArray

      for (i in 0 until body.size()) {
        val obj = body.getJsonObject(i)
        logger.info(obj.toString())
        val id = obj.getValue("id")
        STATUS.com.execute(id.toString(), obj.getString("value"))
      }
      var response = routingContext.response()
      response.putHeader("content-type", "text/plain")
      response.end("COM run")
    }

    router.get("/stdt/status").handler { routingContext ->
      var response = routingContext.response()
      response.putHeader("content-type", "text/json")
      response.end(STATUS.status().toString())
    }



    //-------------------------------------
//    router.post("/ao/fre").handler { routingContext ->
//      val body = routingContext.bodyAsJson
//      val freq = body.getDouble("value")
//      val chan = body.getInteger("channel")
//      if(freq > 0 && freq < 25){
//        STATUS.stations[chan].freq = freq
//      }
//      var response = routingContext.response()
//      response.putHeader("content-type", "text/plain")
//      response.end("OK")
//    }
//
//    router.post("/ao/amp").handler { routingContext ->
//      val body = routingContext.bodyAsJson
//      val ampl = body.getDouble("value")
//      val chan = body.getInteger("channel")
//      if(ampl in 0.0..10.0){
//        STATUS.stations[chan].amp = ampl
//      }
//      var response = routingContext.response()
//      response.putHeader("content-type", "text/plain")
//      response.end("OK")
//    }
//
//    router.get("/ao/status").handler { routingContext ->
//      var response = routingContext.response()
//      response.putHeader("content-type", "text/json")
//      response.end(STATUS.status().toString())
//    }

    server.requestHandler(router).listen(8080) { res ->
      if (res.succeeded()) {
        logger.info("Server is now listening!")
      } else {
        logger.severe("Failed to bind!"+res.cause())
      }
    }
  }

}
