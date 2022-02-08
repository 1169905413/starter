package com.lj.starter.msft


import Automation.BDaq.*
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import org.influxdb.annotation.Measurement
import java.util.logging.Logger

class PCI(val channel:Int, val name:String,val measurement:String) : AbstractVerticle() {
  private val logger: Logger = Logger.getLogger(this.toString())
  val clock = 2000.0
//  val channel = 6
  val section_len = 60
  val buffer_len = section_len * channel

  var vr: ValueRange = ValueRange.V_Neg10To10
  var wfai: WaveformAiCtrl = WaveformAiCtrl()
  var dtns = wfai.supportedDevices

  override fun start(startPromise: Promise<Void>?) {
    logger.info("Deploy PCI $name")
//    dtns.forEach{println(it)}
//    println(dtns[1].DeviceNumber)
//    println(dtns[0].DeviceNumber)
    val de = DeviceInformation(name)
    wfai.selectedDevice = de
//    println("AAA" + de.Description)
//    println("BBB" + wfai.selectedDevice.Description)
    for (aiChannel in wfai.channels) {
      aiChannel.signalType = AiSignalType.SingleEnded
      aiChannel.valueRange = vr
      println(aiChannel)
    }
//    println(wfai.device.description)
//    println(wfai.device.deviceNumber)
//
//    println(wfai.channelCount)
    wfai.conversion.clockRate = clock
    wfai.conversion.channelStart = 0
    wfai.conversion.channelCount = channel
    wfai.record.sectionLength = section_len
    wfai.record.sectionCount = 0
    wfai.addDataReadyListener { sender, args ->
      var remainingCount = args.Count
      val returnedCount = IntByRef()
      var getDataCount = 0
      val buffer = DoubleArray(buffer_len)
      do {
        getDataCount = Math.min(buffer_len, remainingCount)
        val errorCode = wfai.GetData(getDataCount, buffer, 0, returnedCount)
        remainingCount -= returnedCount.value
      } while (remainingCount > 0)
      DEVICE.process(buffer,getdata,measurement)
    }
    wfai.Prepare()
    wfai.Start()
  }

  val getdata:(DoubleArray, Int , Int)->Double = {d, i ,c ->
    d[i * channel + c] }
}
