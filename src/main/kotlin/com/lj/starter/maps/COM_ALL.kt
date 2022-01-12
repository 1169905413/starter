package com.lj.starter.maps
import CRC16
import com.lj.starter.Modbus
import com.lj.starter.Modbus.b2i
import com.lj.starter.Modbus.msg
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.json.JsonObject
import jssc.SerialPort
import java.util.logging.Logger

class COM_ALL(val calcute:COM) :  AbstractVerticle() {
  val step = 1000L
  val logger = Logger.getLogger(this.toString())
  val get_temp = msg(0,4,0,0,0,6,0x71,0xD9)
  val press_buffer = mutableListOf<Byte>()
  var ass = ""
  var a=0
  var b=0
  var c = 0
  var substring =0.0
  var substring1 =0.0
  var com = JsonObject()
  override fun start(startPromise: Promise<Void>) {
//      ADBridge.ad_init()
//      temp.baudrate = SerialPort.BAUDRATE_9600
//      pressure.baudrate = SerialPort.BAUDRATE_9600
//      meter.baudrate = SerialPort.BAUDRATE_115200
    calcute.baudrate = SerialPort.BAUDRATE_38400
    calcute.parity = SerialPort.PARITY_EVEN
//    vertx.deployVerticle(temp)
//    vertx.deployVerticle(meter)
//    vertx.deployVerticle(pressure)
    vertx.deployVerticle(calcute)
//    vertx.setPeriodic(1000){
//      var come_msg = msg(1, 3, 1, 0x5E, 0, 2, 0xA4, 0x25)
//      var comr_msg = msg(3, 3, 1, 0x5E, 0, 2, 0xA5, 0xC7)
//      calcute.execute2("come",come_msg)
//      calcute.execute2("comr",comr_msg)
//      var come = calcute.getRawResult("come")
//      var comr = calcute.getRawResult("comr")
//      if(b==1&&STATUS.station.com_K==0){
//        STATUS.station.com_K = b2i(come?.get(4 - 1)!!.toByte(), come?.get(5 - 1)!!.toByte())
//        STATUS.station.com_R = b2i(comr?.get(4 - 1)!!.toByte(), comr?.get(5 - 1)!!.toByte())
//        println("STATUS.station.com_R====${STATUS.station.com_R}")
//        println("STATUS.station.com_K====${STATUS.station.com_K}")
//      }
//    }



    var come_msg = msg(1, 3, 1, 0x5E, 0, 2, 0xA4, 0x25)
    var comb_msg = msg(2, 3, 1, 0x5E, 0, 2, 0xA4, 0x16)
    var comr_msg = msg(3, 3, 1, 0x5E, 0, 2, 0xA5, 0xC7)
    var com_all = mutableListOf<ByteArray>()
    com_all.add(come_msg)
    com_all.add(comb_msg)
    com_all.add(comr_msg)
    var num = 0
    vertx.setPeriodic(300){
      calcute.execute2("com$num",com_all[num])
      var comb = calcute.getRawResult("com$num")
      com.put("com$num",b2i(comb?.get(3 - 1)!!.toByte(), comb?.get(4 - 1)!!.toByte()))
      num++
      if(num==3){
        num=0
      }
    }
//      meter.port!!.openPort()
//      vertx.setPeriodic(step){
//          temp.execute2("gettemp", get_temp)
//              val getTemp = temp?.getRawResult("gettemp")
//              if(getTemp != null) {
//                  for(b in getTemp){
//                      press_buffer.add(b)
//                      if(press_buffer.size == 17&&press_buffer[1].toString()=="4") {
//                          val value = ((press_buffer[3] + press_buffer[4] + 256) / 10.0)
//                          STATUS.stations[0].temp = value
//                          press_buffer.clear()
//                      }else{
//                          press_buffer.forEach{ print("${it}\t") }
//                          println()
//                      }
//                  }
//              }
//          temp.execute2("temp1", get_temp)
//          var temp_raw = temp.getRawResult("temp1")
//          if(temp_raw != null && temp_raw.size >= 9 ){
//              val value = Modbus.b2i(temp_raw[3-1] , temp_raw[4-1]).toDouble() / 10
//              STATUS.station.temp = value
//          }
//      }
//    var count = 0
//    vertx.setPeriodic(step){
//      count++
//      when {
//          count == 5 ->{
//            meter.execute2("mode","SD,ME,1,02")
//            meter.execute2("start_distance_min","U1")
//          }
//
//          count == 6 -> {
//            meter.execute2("distance_min","L1,1")
//            meter.execute2("mode","SD,ME,1,00")
////            count = 0
//          }
//
//          count == 10 -> {
//            meter.execute2("mode","SD,ME,1,01")
//            meter.execute2("start_distance_max","U1")
//
//            //        count = 0
//          }
//
//          count > 10 -> {
//            meter.execute2("distance_max","L1,1")
//            meter.execute2("mode","SD,ME,1,00")
//            count = 0
//              val distanceBaseOri = STATUS.com.meter.getStringResult("distance_min")
//              val distanceMaxOri = STATUS.com.meter.getStringResult("distance_max")
//              if (distanceBaseOri != null&&distanceMaxOri!=null) {
//                  substring = distanceBaseOri.substring(3, distanceBaseOri.indexOf(",GO")).toDouble()
//                  substring1 = distanceMaxOri.substring(3, distanceMaxOri.indexOf(",GO")).toDouble()
//              }
//          }
//          else -> {
//            meter.execute2("distance","M1,1")
//          }
//      }
//    }


//    vertx.setPeriodic(step * 10 ){
//      leftMotor.execute2("motor","g r0xa6")
//      leftMotor.execute2("amplifier","g r0x20")
//      rightMotor.execute2("motor","g r0xa6")
//      rightMotor.execute2("amplifier","g r0x20")
//    }
//    vertx.setPeriodic(step){
////      logger.info(meter.result.toString()
//      val distance = meter.getStringResult("distance")
//      val distance_max = meter.getStringResult("distance_max")
//      val distance_min = meter.getStringResult("distance_min")
//      if(distance_max != null && distance_max.isNotEmpty() && distance_max!="---,-----") {
//        val dis_max = distance_max.split(",")[1].toDouble()
//        STATUS.stations[0].distance_max_ori = dis_max
//      }
//
//      if(distance_min != null && distance_min.isNotEmpty() && distance_min!="---,-----") {
//        val dis_min = distance_min.split(",")[1].toDouble()
//        STATUS.stations[0].distance_base_ori = dis_min
//      }
//      if(distance != null && distance.isNotEmpty() && distance!="---,-----") {
//            val dis = distance.split(",")[1].toDouble()
//        STATUS.stations[0].distance_ori = dis
//      }
//    }
    super.start(startPromise)
  }

//    fun setTemp(tempValue:Int) {
//        val tempH = Modbus.hbyte(tempValue)
//        val tempL = Modbus.lbyte(tempValue)
//        val woCRC = byteArrayOf(0,6,0,0,tempH,tempL,0,0)
//        val CRC = CRC16()
//        CRC.update(woCRC,0,6)
//        val crc = CRC.value.toInt()
//        val crcH = Modbus.hbyte(crc)
//        val crcL = Modbus.lbyte(crc)
//        woCRC[6] = crcL
//        woCRC[7] = crcH
//        temp.execute2("settemp", woCRC)
//    }

  fun getPos(r:Boolean) = if(r)ADBridge.apos()else ADBridge.bpos()
  fun setPos(pos:Double, r:Boolean) {
    if(r)ADBridge.setapos(pos)else ADBridge.setbpos(pos)
  }

  fun movePos(pos:Double, r:Boolean) {
    setPos(getPos(r) + pos , r)
  }

  fun start_motor(){
//    home()
    ADBridge.ad_motor_start()
  }

  fun home(){
    ADBridge.setapos(0.0)
    ADBridge.setbpos(0.0)
  }

  fun motor_on(){
    ADBridge.ad_motor_on()
  }

  fun motor_off(){
    ADBridge.ad_motor_off()
  }


  fun stop_motor(){
    ADBridge.ad_motor_stop()
    home()
  }

  fun set_param(freq:Double,amp:Double){
    ADBridge.ad_set_param(freq, amp *1000)
  }

//  fun set_param2(freq:Double,amp:Double,amp2:Double){
//      println("start")
//    ADBridge.ad_set_param2(freq, amp *1000,amp2*1000)
//  }

  fun execute(id:String,cmd:String){

    val r = when(id) {
      "0" -> false
      "1" -> true
      else ->true
    }

    val pos = when(cmd) {
      "i r0 20" -> -100.0
      "i r0 21" -> -200.0
      "i r0 22" -> -300.0
      "i r0 23" -> -400.0
      "i r0 24" -> -500.0


      "i r0 10" -> 100.0
      "i r0 11" -> 200.0
      "i r0 12" -> 300.0
      "i r0 13" -> 400.0
      "i r0 14" -> 500.0
      else ->0.0
    }

    movePos(pos,r)
//    logger.info("$newid  $newcmd")
//    exe.execute2(newid, newcmd)
//    exe.execute2(newid, "t 1")
  }
}
