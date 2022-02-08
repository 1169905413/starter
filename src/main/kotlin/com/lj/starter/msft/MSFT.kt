package com.lj.starter.msft


import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import java.util.logging.Logger

object MSFT :AbstractVerticle() {
  val logger = Logger.getLogger(this.toString())
  val channel = 12
  override fun start(startPromise: Promise<Void>) {
    val listToDeploy = mutableListOf<AbstractVerticle>(
      CACHE, INFLUX, DEVICE, PCI(channel, "ai#1", "raw"), PCI(channel, "ai#2", "raw1"), COM, HTTP
    )
    listToDeploy.forEach { vertx.deployVerticle(it) }
  }

}
