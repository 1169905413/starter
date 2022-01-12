package com.lj.starter.maps


import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import java.util.logging.Logger

object MAPS : AbstractVerticle(){
  private val logger: Logger = Logger.getLogger(this.toString())
  var start = -1L

  override fun start(startPromise: Promise<Void>) {
    val listToDeploy = mutableListOf<AbstractVerticle>(
        HTTP,
        STATUS.mc,
        STATUS.com
    )

    listToDeploy.forEach { vertx.deployVerticle(it) }

  }
}
