package com.lj.starter.hvdt

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.json.JsonArray
import java.util.logging.Logger

object HVDT :AbstractVerticle(){
  private val logger: Logger = Logger.getLogger(this.toString())
  var start = -1L

  override fun start(startPromise: Promise<Void>) {
    val listToDeploy = mutableListOf<AbstractVerticle>(

      PCI_AI("ai#1",0,"raw")
      ,PCI_AI("ai#2",1,"raw1")
      ,CAN2
//      ,PCI_AO("ao#1",0)
      ,HTTP
    )

    listToDeploy.forEach { vertx.deployVerticle(it) }
    INFLUX.init()
    STATUS.stations.forEach{station->
      vertx.deployVerticle(Timer(station))
      station.initialCycle()
    }
  }
}

class Timer(val station:STATION):AbstractVerticle(){
  override fun start(startPromise: Promise<Void>) {
    vertx.setPeriodic(2000){
      station.updateamp()
    }
  }
}
