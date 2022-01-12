package com.lj.starter.stdt


import Automation.BDaq.*
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import java.util.logging.Logger


class PCI_AI(val name:String,val id:Int, val measurement:String) : AbstractVerticle() {
  private val logger: Logger = Logger.getLogger(this.toString())
  var start = -1L

//  val clock = AI.clock
//  val channel =  AI.channel
//  val section_len = AI.section_len
//  val buffer_len = AI.buffer_len

  var wfai: WaveformAiCtrl = WaveformAiCtrl()
  override fun start(startPromise: Promise<Void>?) {
    logger.info("Deploy PCI $name")
    wfai.selectedDevice =  DeviceInformation(name)
    for (aiChannel in wfai.channels) {
      aiChannel.signalType = AiSignalType.SingleEnded
      aiChannel.valueRange = AI.vr
    }

    wfai.conversion.clockRate = AI.clock
    wfai.conversion.channelStart = 0
    wfai.conversion.channelCount = AI.channel
    wfai.record.sectionLength = AI.section_len
    wfai.record.sectionCount = 0
    val base = id * AI.station

    wfai.addDataReadyListener { _, args ->
      var remainingCount = args.Count
      val returnedCount = IntByRef()
      var getDataCount = 0
      val buffer = DoubleArray(AI.buffer_len)
      do {
        getDataCount = AI.buffer_len.coerceAtMost(remainingCount)
        wfai.GetData(getDataCount, buffer, 0, returnedCount)
        remainingCount -= returnedCount.value
      } while (remainingCount > 0)
      if(coordinate()){
        STATUS.update(buffer,start, base ,measurement)
        forward()
      }
    }
    wfai.Prepare()
    wfai.Start()
  }

  private fun coordinate():Boolean{
    if (start == -1L) {
      start = System.currentTimeMillis() * 1000
    }
    val diff = System.currentTimeMillis() * 1000 - start
    if (diff > AI.round) {
      logger.severe("slower for $diff us, put ahead clock for $diff us")
      start += diff
    }
    else if(diff < -AI.round){
      logger.severe("faster for 1s, discard 1s data")
      return false
    }
    return true
  }

  private fun forward(){
    start += AI.round
  }

}

