package com.lj.starter.msft

import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonArray
import java.util.logging.Logger

object CACHE :  AbstractVerticle() {
  val data = JsonArray()
  val maxsize =100
  val fetchsize = 21
  val time = 1000L
  val logger = Logger.getLogger(this.toString())


  override fun start() {
    vertx.setPeriodic(time) {
      var d =  data.size() - maxsize
      if(d > 0) {
        logger.info("Purge cache for :$d elements")
        while((d > 0) and (data.size() != 0)) {
          data.remove(0)
          d -= 1
        }
      }
    }
  }

  fun fetch():JsonArray {
    val result = JsonArray()
    var remains = fetchsize
    while((remains > 0) and (data.size() != 0)) {
      result.add(data.remove(0))
      remains -= 1
    }

    return result
    }

  fun add(e:JsonArray) {
//    logger.info(data.size().toString())
    data.add(e)
  }

}
