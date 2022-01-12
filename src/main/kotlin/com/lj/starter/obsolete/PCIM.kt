package com.lj.starter.obsolete

import Clib
import com.sun.jna.Native
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.json.JsonObject
import java.util.logging.Logger

object PCIM: AbstractVerticle() {
  private val logger: Logger = Logger.getLogger(this.toString())
  var amps = mutableListOf<Double>(0.0,0.0,0.0,0.0,0.0,0.0)
  var freqs = mutableListOf<Double>(10.0,10.0,10.0,10.0,10.0,10.0)
  val step = 3L
  var phases = mutableListOf<Double>(0.0,0.0,0.0,0.0,0.0,0.0)

  override fun start(startPromise: Promise<Void>?) {
    logger.info("Deploy PCIM")
    val lib: Clib = Native.loadLibrary(
      "lib\\dll2.dll",
      Clib::class.java
    )

    lib.Init();

    vertx.setPeriodic(step) {
      for (i in 0 until amps.size) {
        val amp = amps[i]
        val cycle = 1000 / freqs[i]
        var phase = phases[i]
        if(phase >= 1) phase -=1
        phases[i] = phase + step / cycle
        lib.Aout(Math.sin(phase * 2 * Math.PI) * amp , i)
      }
    }
  }

  fun status (): JsonObject {
    return JsonObject()
      .put("amps", amps)
      .put("freqs", freqs)
  }
}
