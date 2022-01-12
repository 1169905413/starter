package com.lj.starter.maps


import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import jssc.SerialPort
import jssc.SerialPortEvent
import jssc.SerialPortEventListener
import jssc.SerialPortList
import java.util.logging.Logger

class COM(val name:String?,val append:Boolean = true) :  AbstractVerticle() {
  val logger = Logger.getLogger(this.toString())
  val list = mutableListOf<Pair<String, ByteArray>>()
//  val list2 = mutableListOf<Pair<String, ByteArray>>()


  val result = mutableMapOf<String,List<Byte>>()
  var baudrate :Int = SerialPort.BAUDRATE_115200
  var databits :Int = SerialPort.DATABITS_8
  var stopbits :Int = SerialPort.STOPBITS_1
  var parity :Int = SerialPort.PARITY_NONE
  val freq = 50L
//  val update_freq = 500L
  var port: SerialPort? = null
  var id ="NA"

  override fun start(startPromise: Promise<Void>?) {
    logger.info("Deploy COM $name")
      this.connect()
    vertx.setPeriodic(freq) {
      if (list.isNotEmpty()) {
        try{
          val element = list.removeAt(0)
          var c = element.second
          id = element.first
          if (port != null && port!!.isOpened) {
//              if(append) {
//                  val last = c.takeLast(2)
//                if(last.size < 2 || last[0]!='\r'.toByte() || last[1] != '\n'.toByte()){
////            if (!c.endsWith("\r\n")) {
////              c+= "\r\n"
//                  c = c.plus('\r'.toByte()).plus('\n'.toByte())
//                }
//              }
            port!!.writeBytes(c)
          } else {
            result[id] = "Port not open".toByteArray().asList()
          }
        }catch (e:Exception){
          e.printStackTrace()
        }

      }
    }
//    vertx.setPeriodic(2000){
//      logger.info(name + result.toString())
//    }
//    vertx.setPeriodic(update_freq) {
//      execute2("cycle", "i r30")
//      execute2("motor", "g r0xa6")
//      execute2("controller", "g r0x20")
//    }
    super.start(startPromise)
  }

  var value = ""
  fun connect() {
    if(port !=null && port!!.isOpened){
      logger.info("port already opened!")
      return
    }
    try {
      port = if (name == null) SerialPort(SerialPortList.getPortNames()[0]) else SerialPort(name)
      port!!.openPort()
      port!!.setParams(
        baudrate,
        databits,
        stopbits,
        parity
      )
      port!!.addEventListener(COMListner(this))
    }catch (e:Exception){
      e.printStackTrace()
    }
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
    list.add(id to command.toByteArray())
  }
  fun execute2(id:String, command:ByteArray) {
//    println(list)
    list.add(id to command)
  }

  fun close() {
    port?.closePort()
  }

  fun getRawResult(key:String):List<Byte>?{
//    println(result)
    if(result.containsKey(key)){
      return result[key]
    }
    return null
  }

  fun getStringResult(key:String):String?{
    val r = getRawResult(key)
    return if(r != null) String(r.toByteArray()).trim()
    else {
      null
    }
  }

  fun isOpen() = port?.isOpened
  fun ports() = SerialPortList.getPortNames()

}


class COMListner(val com:COM) : SerialPortEventListener {
  override fun serialEvent(event: SerialPortEvent) {
    try{
      var s:MutableList<Byte> = mutableListOf()
      val start = System.currentTimeMillis()
//      val id = com.id

//      com.port!!.read
      if(!com.append){
        var bs:ByteArray? = null
//        do{
          bs = com.port!!.readBytes()
          if(bs != null){
            for (b in bs){
              s.add(b)
            }
          }
//        }while(bs != null && bs.isNotEmpty())

      }


      do {
        var b = com.port!!.readBytes(1)[0]
        var spend = System.currentTimeMillis() - start

//        if(id =="temp1"){
//          println(b)
//          println(spend)
//        }

//        if(b == 0.toByte())
//          break
//        println(b)
        s.add(b)
      }
      while(b != 13.toByte() && spend < 100)
//      com.logger.info("${com.name} ==> result:$s id:${com.id}")
      if(s.isNotEmpty())
        com.result[com.id] = s
//      com.logger.info( com.result.toString())
    }catch (e:Exception){
      e.printStackTrace()
    }
  }
}
