package com.lj.starter.maps


import com.lj.starter.Modbus
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
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
    router.get("/pushed/gpreseterror").handler {
      val advGpResetError = ADBridge.AdvGpResetError()
      val response = it.response()
      response.putHeader("content-type", "text/plain")
      response.end("ok")
    }

    router.get("/pushed/getagitonum").handler {
      var json = JsonObject()
      json.put("num",STATUS.station.agito_num)
      val response = it.response()
      response.putHeader("content-type", "text/plain")
      response.end(json.toString())
    }
    router.get("/pushed/getsv").handler {
      val advdoGetBit0 = ADBridge.AdvdoGetBit(0, 6)
      val advdoGetBit1 = ADBridge.AdvdoGetBit(1, 6)
      val advdoGetBit2 = ADBridge.AdvdoGetBit(2, 6)
      var json = JsonObject()
      json.put("sv0",advdoGetBit0)
      json.put("sv1",advdoGetBit1)
      json.put("sv2",advdoGetBit2)
      val response = it.response()
      response.putHeader("content-type", "text/plain")
      response.end(json.toString())
    }
    router.get("/pushed/svoff").handler {
      val bodyAsJson = it.bodyAsJson
      ADBridge.Advsvoff()
      val response = it.response()
      response.putHeader("content-type", "text/plain")
      response.end("ok")
    }
    router.get("/pushed/svon").handler {
      val bodyAsJson = it.bodyAsJson
      ADBridge.Advsvon()
//      ADBridge.AdvdoSetBit(bodyAsJson.getInteger("sv"),6,bodyAsJson.getBoolean("svbool"))
//      ADBridge.AdvdoSetBit(1, 6,bodyAsJson.getBoolean("sv1"))
//      ADBridge.AdvdoSetBit(2, 6,bodyAsJson.getBoolean("sv2"))
//      Thread.sleep(50)
//      val advdoGetBit = ADBridge.AdvdoGetBit(bodyAsJson.getInteger("sv"), 6)
//      val advdoGetBit1 = ADBridge.AdvdoGetBit(1, 6)
//      val advdoGetBit2 = ADBridge.AdvdoGetBit(2, 6)
//      var json = JsonObject()
//      json.put("sv0",advdoGetBit0)
//      json.put("sv1",advdoGetBit1)
//      json.put("sv2",advdoGetBit2)
      val response = it.response()
      response.putHeader("content-type", "text/plain")
      response.end("ok")
    }
    router.post("/pushed/setsv").handler {
      val sv = it.bodyAsJson.getInteger("sv")
      val svbool = it.bodyAsJson.getBoolean("svbool")
      ADBridge.AdvdoSetBit(sv, 6, svbool)
      val response = it.response()
      response.putHeader("content-type", "text/plain")
      response.end("ok")
    }
    router.get("/pushed/getpt").handler {
      COM("COM5").port?.writeString("as")
      val readString = COM("COM5").port?.readString()
      println(readString)
      val response = it.response()
      response.putHeader("content-type", "text/plain")
      response.end(readString.toString())
    }
    router.post("/pushed/calculate").handler {
    STATUS.station.com_k1 = STATUS.com.com["com2"]
    var body = it.bodyAsJson
      STATUS.mc.num = body.getDouble("num")
      STATUS.mc.pipe = body.getDouble("pipe")
      val response = it.response()
      response.putHeader("content-type", "text/plain")
      response.end("ok")
    }
    router.get("/pushed/AdvOpenAxis").handler {
      val advOpenAxis = ADBridge.AdvOpenAxis()
      val response = it.response()
      response.putHeader("content-type", "text/plain")
      response.end(advOpenAxis.toString())
    }
    router.post("/pushed/zero").handler {
      STATUS.station.count_base=0
      for(i in 0 .. STATUS.station.act_pos.size){
        STATUS.station.act_count.set(i,0)
      }
      val response = it.response()
      response.putHeader("content-type", "text/plain")
      response.end("OK")
    }
    router.post("/pushed/setmeter").handler {
      var num = it.bodyAsJson.getInteger("num")
      STATUS.station.count_base=num
      STATUS.station.act_count = arrayListOf(0,0,0)
      val response = it.response()
      response.putHeader("content-type", "text/plain")
      response.end("OK")
    }
    router.get("/pushed/errorreset/:num").handler {
      val num = it.request().getParam("num").toInt()
//      vertx.deployVerticle(STATUS.mc)
      ADBridge.AdvResetError(num)
      ADBridge.AdvdoSetBit(num,4, true)
      Thread.sleep(1000)
      ADBridge.AdvdoSetBit(num,4, false)
      val response = it.response()
      response.putHeader("content-type", "text/plain")
      response.end("OK")
    }
//    router.get("/pushed/test/:channel/:bool").handler {
//      var channel = it.request().getParam("channel").toInt()
//      var bool = it.request().getParam("bool").toBoolean()
//      ADBridge.AdvdoSetBit(channel,bool)
//      val response = it.response()
//      response.putHeader("content-type", "text/plain")
//      response.end("OK")
//    }
//    router.get("/pushed/reset").handler {
//      ADBridge.AdvResetError(0)
//      ADBridge.AdvResetError(1)
//      ADBridge.AdvResetError(2)
//      ADBridge.AdvdoSetBit(0,4, true)
//      ADBridge.AdvdoSetBit(1,4, true)
//      ADBridge.AdvdoSetBit(2,4, true)
//      Thread.sleep(1000)
//      ADBridge.AdvdoSetBit(0,4, false)
//      ADBridge.AdvdoSetBit(1,4, false)
//      ADBridge.AdvdoSetBit(2,4, false)
//      val response = it.response()
//      response.putHeader("content-type", "text/plain")
//      response.end("ok")
//    }
    router.post("/pushed/start").handler{
      STATUS.station.com_k2 = STATUS.com.com["com2"]
      val body = it.bodyAsJson
      val startMove = STATUS.mc.start_move(body)
      if(startMove.size()==0){
        startMove.put("nb1",0)
      }
      val response = it.response()
      response.putHeader("content-type", "text/plain")
      response.end(startMove.toString())
    }
    router.post("/pushed/stop").handler{
      STATUS.station.com_k1=0
      STATUS.station.com_k2=0
      STATUS.station.com_R=0
      STATUS.station.com_K=0
      STATUS.com.b=0
      STATUS.com.c=0
      STATUS.mc.stop_move()
      val response = it.response()
      response.putHeader("content-type", "text/plain")
      response.end("OK")
    }
    router.post("/pushed/setpos").handler{
      val body = it.bodyAsJson
      val axis = body.getInteger("axis")
      val pos = body.getDouble("pos")
      var json = JsonObject()
      val response = it.response()
      if(axis==0) {
        if (STATUS.station.com_K + (pos / 1000 * 16) < (-23) || STATUS.station.com_K + (pos / 1000 * 16) > 80) {
          json.put("nb1","Rotation amplifier exceeded limitation.")
          response.putHeader("content-type", "text/")
          response.end(json.toString())
        }else{
          STATUS.mc.set_pos(axis, pos)
        }
      }
       if(axis==1) {
          if (STATUS.station.com_k1 + (pos / 1000 * 16) < (-55) || STATUS.station.com_k1 + (pos / 1000 * 16) > 10) {
            json.put("nb1","Extention amplifier exceeded limitation.")
            response.putHeader("content-type", "text/plain")
            response.end(json.toString())
          }else{
            STATUS.mc.set_pos(axis, pos)
          }
        }
      if(axis==2){
          if (STATUS.station.com_R + (pos / 5000 * 16) < (-17) || STATUS.station.com_K + (pos / 5000 * 16) > 15) {
            json.put("nb1","Bend amplifier exceeded limitation.")
            response.putHeader("content-type", "text/plain")
            response.end(json.toString())
          }else{
            STATUS.mc.set_pos(axis, pos)
          }
        }
      response.putHeader("content-type", "text/plain")
      response.end("OK")
    }

    router.post("/pushed/sethome").handler{
      val body = it.bodyAsJson
      val axis = body.getInteger("axis")
      STATUS.mc.set_home(axis)
      val response = it.response()
      response.putHeader("content-type", "text/plain")
      response.end("OK")
    }
    router.post("/pushed/station").handler{
      val body = it.bodyAsJson
      val values = body.getJsonArray("values")
     STATUS.station.update(values)
      val response = it.response()
      response.putHeader("content-type", "text/plain")
      response.end("OK")
    }
    router.get("/pushed/status").handler { routingContext ->
      var response = routingContext.response()
      response.putHeader("content-type", "text/json")
      response.end(STATUS.status().toString())
    }
    router.get("/pushed/data").handler { routingContext ->
      var response = routingContext.response()
      response.putHeader("content-type", "text/json")
      response.end(STATUS.mc.list4.toString())
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
