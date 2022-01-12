package com.lj.starter.lvdt

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import java.util.logging.Logger

object LVDT:AbstractVerticle() {
  val logger = Logger.getLogger(this.toString())

  override fun start(startPromise: Promise<Void>) {
    val listToDeploy = mutableListOf<AbstractVerticle>(
       INFLUX,TCP, HTTP
    )
    listToDeploy.forEach { vertx.deployVerticle(it) }
  }

}
