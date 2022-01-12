package com.lj.starter.stdt


import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import java.util.logging.Logger

object STDT : AbstractVerticle(){
  private val logger: Logger = Logger.getLogger(this.toString())
  var start = -1L

  override fun start(startPromise: Promise<Void>) {
    val listToDeploy = mutableListOf<AbstractVerticle>(
      INFLUX
      , PCI_AI("ai#1",0,"raw")
       ,
      STATUS.com
//      ,STATUS.ao
//      , PCI_AO("ao#1",0)
      , HTTP
    )

    listToDeploy.forEach { vertx.deployVerticle(it) }

//    val state = INFLUX.query_state()
//    STATUS.stations[0].cycle_AI = state[0]
//    STATUS.stations[0].passCycle_distance = state[0]
//    STATUS.stations[0].passCycle_press = state[0]

  }
}
