package com.lj.starter.maps

import io.vertx.core.json.JsonObject

object STATUS {
  val station:STATION = STATION()
  val mc:MotionCard = MotionCard(station)
  val com =COM_ALL(COM("COM10"))
  fun status (): JsonObject {
    try{

      return JsonObject.mapFrom(station)
    }catch (e:Exception){
      e.printStackTrace()
    }
    return JsonObject()
  }

  fun data (): JsonObject {
    try{
      return JsonObject.mapFrom(station.data)
    }catch (e:Exception){
      e.printStackTrace()
    }
    return JsonObject()
  }
}
