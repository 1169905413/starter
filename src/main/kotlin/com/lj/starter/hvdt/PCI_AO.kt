package com.lj.starter.hvdt


import Automation.BDaq.*
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise

import java.util.logging.Logger

class PCI_AO(private val name:String, private val id:Int) : AbstractVerticle() {

  private val logger: Logger = Logger.getLogger(this.toString())
  private val buffer = DoubleArray(AO.buffer_len)
  private val bao: BufferedAoCtrl = BufferedAoCtrl()
  private val offset = id * AO.channel

  override fun start(startPromise: Promise<Void>?) {
    logger.info("Deploy PCI_AO : $name")
    bao.selectedDevice =  DeviceInformation(name)
    for (aiChannel in bao.channels) {
      aiChannel.valueRange = AO.vr
    }
    bao.scanChannel.channelStart = 0
//    bao.scanChannel.
    bao.scanChannel.channelCount = AO.channel
    bao.scanChannel.samples = AO.section_len
    bao.scanChannel.intervalCount = AO.section_len
    bao.convertClock.rate = AO.clock
    bao.Prepare()

    vertx.setPeriodic(AO.step) {
      for (chan in 0 until  AO.channel){
          val index = chan + offset
          STATUS.stations[index].genWave(buffer,chan)
      }
      bao.SetData(AO.buffer_len, buffer)
      bao.RunOnce()
    }
  }
}


