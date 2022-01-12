package com.lj.starter.stdt

import CRC16
import com.lj.starter.Modbus
import com.lj.starter.Modbus.msg
import com.lj.starter.hvdt.CAN2
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.kotlin.core.deployVerticleAwait
import jssc.SerialPort
import java.util.logging.Logger

class COM_ALL(val meter:COM, val temp:COM, val pressure:COM?) :  AbstractVerticle() {
  val step = 1000L
  val logger = Logger.getLogger(this.toString())
  val msg = msg(0, 4, 0, 0, 0, 6, 0x71, 0xD9)
  val get_press_msg = msg(1,3,0,0x6a,0,2,0xe4,0x17)
  val press_buffer = mutableListOf<Byte>()

  override fun start(startPromise: Promise<Void>) {

    temp.baudrate = SerialPort.BAUDRATE_9600
    pressure?.baudrate  = SerialPort.BAUDRATE_9600
    vertx.deployVerticle(temp)
    vertx.deployVerticle(pressure)
    vertx.deployVerticle(meter)



    ADBridge.ad_init()
    ADBridge.ad_motor_on()
    home()

    var count = 0
    vertx.setPeriodic(step){
      count++

      temp.execute2("temp1", msg)
      pressure?.execute2("get_press", get_press_msg)


      when {
          count == 5 ->{
            meter.execute2("mode","SD,ME,1,02")
            meter.execute2("start_distance_min","U1")
          }

          count == 6 -> {
            meter.execute2("distance_min","L1,1")
            meter.execute2("mode","SD,ME,1,00")
//            count = 0
          }

          count == 10 -> {
            meter.execute2("mode","SD,ME,1,01")
            meter.execute2("start_distance_max","U1")

            //        count = 0
          }

          count > 10 -> {
            meter.execute2("distance_max","L1,1")
            meter.execute2("mode","SD,ME,1,00")
            count = 0
          }

          else -> {
            meter.execute2("distance","M1,1")
          }
      }
    }


//    vertx.setPeriodic(step * 10 ){

//      leftMotor.execute2("motor","g r0xa6")
//      leftMotor.execute2("amplifier","g r0x20")
//      rightMotor.execute2("motor","g r0xa6")
//      rightMotor.execute2("amplifier","g r0x20")
//    }




    vertx.setPeriodic(step){

//      logger.info(meter.result.toString())
//      val motor = ADBridge.readInport() shl 14
      val motor = 0
      STATUS.stations[0].left_motor = if(motor % 2 == 0) "v 4095" else "v nok"
      STATUS.stations[0].right_motor = if(motor <= 1) "v 4095" else "v nok"
      if(motor != 0) {
        stop_motor()
        STATUS.stations[0].start = false
      }

      STATUS.stations[0].left_amplifier="v "+ ADBridge.pwrTemp().toString()
      STATUS.stations[0].right_amplifier="v "+ ADBridge.pwrTemp().toString()


      val distance = meter.getStringResult("distance")
      val distance_max = meter.getStringResult("distance_max")
      val distance_min = meter.getStringResult("distance_min")
      val temp_raw = temp.getRawResult("temp1")
      val press_raw = pressure?.getRawResult("get_press")

      if(press_raw != null) {
//        println(press_raw.size)
        for(b in press_raw){
          press_buffer.add(b)
          if(press_buffer.size == 9){
            val value = Modbus.b2f(press_buffer[ 5].toInt() ,press_buffer[ 6].toInt(),press_buffer[ 3].toInt(),press_buffer[ 4].toInt())
            STATUS.stations[0].press_control = value
            press_buffer.clear()
          }
        }
      }
//      if(press_raw != null && press_raw.size >= 9 ){
//
//      }

//      println(temp_raw)
      if(temp_raw != null && temp_raw.size >= 9 ){
        val value = Modbus.b2i(temp_raw[3-1] , temp_raw[4-1]).toDouble() / 10
        STATUS.stations[0].temp = value
      }


//      val left_motor = leftMotor.result["motor"]
//      val left_amplifier = leftMotor.result["amplifier"]
//      val right_motor = rightMotor.result["motor"]
//      val right_amplifier = rightMotor.result["amplifier"]

//      println("$distance = $distance_max = $distance_min")


      if(distance_max != null && distance_max.isNotEmpty()) {
//        println("max$distance_max")
        val dis_max = distance_max.split(",")[1].toDouble()
        STATUS.stations[0].distance_max_ori = dis_max
      }

      if(distance_min != null && distance_min.isNotEmpty()) {
//        println("min$distance_min")

        val dis_min = distance_min.split(",")[1].toDouble()
        STATUS.stations[0].distance_base_ori = dis_min
      }


      if(distance != null && distance.isNotEmpty()) {
        val dis = distance.split(",")[1].toDouble()
        STATUS.stations[0].distance_ori = dis
//        if(!STATUS.stations[0].start){
//          STATUS.stations[0].distance_base = dis
//        }
      }
//      if(left_motor != null) STATUS.stations[0].left_motor = left_motor
//      if(left_amplifier != null) STATUS.stations[0].left_amplifier = left_amplifier
//      if(right_motor != null) STATUS.stations[0].right_motor = right_motor
//      if(right_amplifier != null) STATUS.stations[0].right_amplifier = right_amplifier

//      STATUS.stations[0].distance = if(meter.result["distance"] == null) STATUS.stations[0].distance else meter.result["distance"]!!
//      STATUS.stations[0].left_motor = if(leftMotor.result["motor"] == null) STATUS.stations[0].distance else leftMotor.result["motor"]!!
//      STATUS.stations[0].left_amplifier = if(leftMotor.result["amplifier"] == null) STATUS.stations[0].distance else leftMotor.result["amplifier"]!!
//      STATUS.stations[0].right_motor = if(rightMotor.result["motor"] == null) STATUS.stations[0].distance else rightMotor.result["motor"]!!
//      STATUS.stations[0].right_amplifier = if(rightMotor.result["amplifier"] == null) STATUS.stations[0].distance else rightMotor.result["amplifier"]!!

    }




    super.start(startPromise)
  }

  fun setTemp(id:Int, tempValue:Int) {
    val idH = Modbus.hbyte(id)
    val idL = Modbus.lbyte(id)
    val tempH = Modbus.hbyte(tempValue)
    val tempL = Modbus.lbyte(tempValue)

    val woCRC = byteArrayOf(0,6, idH,idL , tempH, tempL,0,0)
    val CRC = CRC16()
    CRC.update(woCRC,0,6)
    val crc = CRC.value.toInt()
    val crcH = Modbus.hbyte(crc)
    val crcL = Modbus.lbyte(crc)
    woCRC[6] = crcL
    woCRC[7] = crcH
    temp.execute2("target_temp", woCRC)
//    CANBridge2.Send(0,0, 8,  woCRC, CAN2.temp_channel)
  }


  fun setPressure(id:Int, press:Float) {
    val bits = Modbus.f2b(press)
    val woCRC = byteArrayOf(0x01,0x10,0x00,0x6a,0x00,0x02,0x04,   bits[2].toByte(),bits[3].toByte(),bits[0].toByte(),bits[1].toByte(),    0,0)

    val CRC = CRC16()
    CRC.update(woCRC,0,11)
    val crc = CRC.value.toInt()
    val crcH = Modbus.hbyte(crc)
    val crcL = Modbus.lbyte(crc)
    woCRC[11] = crcL
    woCRC[12] = crcH
    pressure?.execute2("target_pressure", woCRC)
//    CANBridge2.Send(0,0, 8,  woCRC, CAN2.temp_channel)
  }

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

//  fun set_param(freq:Double,amp:Double){
//    ADBridge.ad_set_param(freq, amp *1000)
//  }

  fun set_param2(freq:Double,amp:Double,amp2:Double){
    ADBridge.ad_set_param2(freq, amp *1000,amp2*1000)
  }

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
