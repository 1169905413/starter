package com.lj.starter.msft


import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import jssc.SerialPort
import jssc.SerialPortEvent
import jssc.SerialPortEventListener
import jssc.SerialPortList
import java.util.logging.Logger

object COM :  AbstractVerticle() {
  val logger = Logger.getLogger(this.toString())
  val list = mutableListOf<Pair<String, String>>()
  val result = mutableMapOf<String,String>()

  val freq = 100L
  val update_freq = 500L
  var port: SerialPort? = null
  var id ="NA"



  override fun start(startPromise: Promise<Void>?) {
    logger.info("Deploy COM!")
    vertx.setPeriodic(freq) {
      if (list.isNotEmpty()) {
        val element = list.removeAt(0)
        var c = element.second
        id = element.first
        result[id] = ""
        if (port != null && port!!.isOpened) {
          DEVICE.update(c.trim())
          if (!c.endsWith("\r\n")) {
            c += "\r\n"
          }
          port!!.writeString(c)
        } else {
          result[id] = "Port not open"
        }
      }
    }
    vertx.setPeriodic(update_freq) {
      execute2("cycle", "i r30")
      execute2("motor", "g r0xa6")
      execute2("controller", "g r0x20")
    }
    super.start(startPromise)
  }
  var value = ""
  fun connect(name:String? = null,
              baudrate :Int = SerialPort.BAUDRATE_9600,
              databits :Int = SerialPort.DATABITS_8,
              stopbits :Int = SerialPort.STOPBITS_1,
              parity :Int = SerialPort.PARITY_NONE
              ) {
    if(port !=null && port!!.isOpened){
      logger.info("port already opened!")
      return
    }
    port =  if(name==null) SerialPort(SerialPortList.getPortNames()[0]) else SerialPort(name)
    port!!.openPort()
    port!!.setParams(
      baudrate,
      databits,
      stopbits,
      parity
    )
    port!!.addEventListener(COMListner())
    logger.info("port opened!")
  }

//  fun execute(s:String) {
//    value = ""
//    var c = s
//    if(port!!.isOpened){
//      if(!s.endsWith("\r\n")){
//        c +="\r\n"
//      }
//      port!!.writeString(c)
//    } else {
//      value = "com not open!"
//    }
//  }

  fun execute2(id:String, command:String) {
    list.add(id to command)
  }

  fun close() {
    port?.closePort()
  }

  fun isOpen() = port?.isOpened

  class COMListner() : SerialPortEventListener {
    override fun serialEvent(event: SerialPortEvent) {
      var s = ""
      do {
        var b = port!!.readBytes(1)[0]
        if(b == 0.toByte())
          break
        s += b.toChar()
      }
      while  (b != 13.toByte())
      result[id] = s
    }
  }

  fun ports() = SerialPortList.getPortNames()
}
