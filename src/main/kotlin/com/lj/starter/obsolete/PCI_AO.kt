package com.lj.starter.obsolete


import Automation.BDaq.*
import com.lj.starter.stdt.AO
import com.lj.starter.stdt.STATION
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
    bao.streaming = true
    bao.Prepare()

//    vertx.setPeriodic(AO.step-75-25) {
//      for (chan in 0 until  AO.station){
//          val index = chan + offset
//          STATUS.stations[index].genWave(buffer)
//      }
//      bao.SetData(AO.buffer_len, buffer)
//      bao.Start()
////      bao.RunOnce()
//    }
  }

  fun prepare(){
    bao.Prepare()
  }

  fun run(){
    bao.Start()
  }

  fun fill(station: STATION){
//    station.genWave(buffer)
    bao.SetData(AO.buffer_len, buffer)
  }

  fun pause() {
    bao.Stop(0)
  }

}


