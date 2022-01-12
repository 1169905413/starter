package com.lj.starter.msft_can_ai

import ByteByReference
import CanBridge
import IntArrayByReference
import com.lj.starter.Modbus
import com.sun.jna.Native
import com.sun.jna.ptr.IntByReference
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import okhttp3.internal.toHexString
import CRC16
import com.fasterxml.jackson.annotation.JsonIgnore
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import java.util.logging.Logger

import com.lj.starter.Modbus.msg
import com.lj.starter.Val
import com.lj.starter.stdt.STATUS
import com.sun.org.apache.xpath.internal.operations.Mod
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

object CAN2: AbstractVerticle() {
  private val logger: Logger = Logger.getLogger(this.toString())
  val step = 1L
  val timeout = 50
  val temp_channel = 1
  val moter_channel = 0

  val addresses = arrayOf(CopleyMotor(0, moter_channel))
  val all_node = SEND_ONLY(0, moter_channel,0x00)
  val motor = CAN_CHANNEL(moter_channel,addresses)

  fun init_all_node(){
    all_node.sendCommand(msg(0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x00))
    addresses.forEach {
      it.sine()
      it.dutycycle()
    }
  }


  override fun start(startPromise: Promise<Void>?) {
    logger.info("Deploy CAN")
    val init = CANBridge2.Init(timeout, 1, 1, intArrayOf(1000))
    logger.info("init = $init")
    if (init) {
      init_all_node()
      vertx.deployVerticle(motor)
    } else {
      logger.severe("Failed to deploy CAN")
    }
  }

}


class CAN_CHANNEL(val chan: Int,val address: Array<out CAN_ADDRESS>, val get_cob:Int = 0x581):AbstractVerticle(){
  val step = 5L
  val len = 256;
  val buffer = ByteByReference(len * 8)
  val num = IntByReference(0)
  val lengths = IntArrayByReference(len)
  val ids = IntArrayByReference(len)
  val cobs = IntArrayByReference(len)
  val logger: Logger = Logger.getLogger(this.toString())

  override fun start() {
    address.forEach {  vertx.deployVerticle(it)}
    vertx.setPeriodic(step) {
      val ret = CANBridge2.Read(len,num,lengths,cobs,ids, buffer, chan)
      if(num.value == 0 || ret != 0){
        logger.fine("No message. Code :$ret")
      } else {
        for (i in 0 until num.value) {
          val l = lengths.getValue(i.toLong())
          val cob = cobs.getValue(i.toLong())
          val id = ids.getValue(i.toLong())
          val frame = ByteArray(l)
          for (j in 0 until l) {
            frame[j] =  b2v(buffer, i, j)
          }
          val new_cob = id - get_cob
          if(new_cob in address.indices){
            address[new_cob].read_frame(frame)
          }else{
            logger.severe("wrong cob id: $id")
          }
        }
      }
    }
    super.start()
  }
  fun b2v(buffer:ByteByReference , i:Int , j:Int) =
    (buffer.getValue((i * 8 + j).toLong()).toInt() and 0xff).toByte()
}

abstract class CAN_ADDRESS(val id:Int, val chan:Int, val send_cob:Int = 0x601): AbstractVerticle() {
  val logger: Logger = Logger.getLogger(this.toString())
  abstract fun read_frame(frame:ByteArray)
  fun sendCommand(msgs:Array<ByteArray>) {
    val cob = id + send_cob
    msgs.forEach {
        msg->  CANBridge2.Send(id, cob, 8,  msg, chan)
    }
  }

  fun sendCommand(msg:ByteArray) {
    val cob = id + send_cob
    CANBridge2.Send(id, cob, 8,  msg, chan)
  }

  fun compare(frame: ByteArray, head:ByteArray):Boolean {
    for(i in 0 until minOf(frame.size,head.size)){
      if(head[i] != frame[i]) return false
    }
    return true
  }

}


class SEND_ONLY(id: Int, chan:Int, send_cob: Int = 0x601):CAN_ADDRESS(id,chan, send_cob) {
  override fun read_frame(frame: ByteArray) {
    TODO("Not yet implemented")
  }
}

class CopleyMotor(id: Int, chan:Int, send_cob: Int = 0x601) : CAN_ADDRESS(id,chan, send_cob) {
  var temp:Double = 0.0
  var state = 0
  var idx = 0
  var base = 0L
  val interval = 533L
  val gap = 200
  val value = Val()
//  val list = mutableListOf<Pair<String, String>>()
  val result = mutableMapOf<String,String>()

  var freq:Int = 1
  var amp:Int = 0
  var amp_adjust = 0
  var auto_adjust = false
  var controller_temp = 0
  var motor_temp = 0
  var pass = false

  var data = mutableListOf<Double>()
  var data2 = listOf<Double>()


  @JsonIgnore
  val positions = arrayListOf<Point>()
  @JsonIgnore
  val temp_pos = arrayListOf<Byte>()

  fun reset() {
    temp_pos.clear()
    positions.clear()
    idx = 0
    state = 0
    this.value.now()
    this.data.clear()

  }

  fun motorData() = JsonObject().put("data", this.data2)
  fun toJson() = JsonObject().put("amp",amp).put("amp_adjust",amp_adjust).put("freq",freq).put("auto_adjust",auto_adjust)

  fun init(){
//    position()
//    sine()
//    dutycycle()
    this.sendCommand(CopleyMsg.trace_channel)
    this.sendCommand(CopleyMsg.trigger_mode)
    this.sendCommand(CopleyMsg.trigger_delay)
  }


  fun update(values: JsonArray){
    for ( i in 0 until values.size()) {
      val kv = values.getJsonObject(i)
      when (val k = kv.getString("key")) {
        "auto_adjust" -> this.auto_adjust = kv.getBoolean("value")
      }
    }

  }



  fun translate(command: String)  {
    this.sendCommand(CopleyMsg.stop_trace)
//    this.init()
    when(command) {
      "g r0xa6" -> this.motor_temp()
      "g r0x20" -> this.controller_temp()
      else -> {
        val split = command.split(" ")
        if(split.size == 3){
          if(split[0] == "i"){
            val c = split[1]
            val v = split[2].trim().toInt()
            when(c){
              "r17" ->this.freq(v )
              "r16" -> this.amp(v * 10)
              "r0" ->{
                when(v) {
                  4 -> this.start_motor()
                  1 -> this.stop_motor()
                  else -> {
                    if(v in (10..14)) this.jog((v-9) * 400)
                    if(v in (20..24)) this.jog((19-v) * 400)
                  }
                }
              }
            }
          }
        }
      }
    }
    this.reset()
  }



  override fun start() {
    super.start()
    this.init()
    vertx.setPeriodic(50L) {
      when (state){
        0 -> {
//          this.init()
          this.sendCommand(CopleyMsg.stop_trace)
          this.sendCommand(CopleyMsg.start_trace)
        }

        1 -> {
          this.sendCommand(CopleyMsg.check_trace)
        }

        2 -> {
          this.sendCommand(CopleyMsg.start_upload)
        }

        3 -> {
          if(positions.size == 0){
            this.sendCommand(CopleyMsg.first_query)
          } else if (idx == 0x7f) {
            idx = 0
            this.sendCommand(CopleyMsg.continue_query)
          }
        }
        4-> {
          this.sendCommand(CopleyMsg.query(idx + 1))
        }
        5-> {
          INFLUX.write(positions)
          this.sendCommand(CopleyMsg.end_upload)
          this.value.now()
          val range = this.value.max - this.value.min

          pass = range >= 1.0
          val g = (amp * 2 - range).toInt()

          this.data2 = this.data.toList()
          if(this.auto_adjust){
            if(g > gap) {
              this.amp_adjust ++
              this.amp(this.amp)
            } else if(g < -gap) {
              this.amp_adjust --
              this.amp(this.amp)
            }

          }

          this.controller_temp()
          this.motor_temp()
          this.reset()

        }
      }
    }
  }

  fun exctract_frame_value(frame: ByteArray) = Modbus.b2i(frame[7],frame[6],frame[5],frame[4])




  override fun read_frame(frame: ByteArray) {
    logger.fine("frame:" + frame.joinToString { "," })
    if(compare(frame,byteArrayOf(0x4b.toByte() ,0x02.toByte(), 0x22.toByte()) )){
      controller_temp = exctract_frame_value(frame) and 0x0000ffff
      result["controller"] = "v $controller_temp"
      return
    }


    if(compare(frame,byteArrayOf(0x4b.toByte() ,0x90.toByte(), 0x21.toByte()) )){
      motor_temp = exctract_frame_value(frame) and 0x0000ffff
      result["motor"] = "v $motor_temp"

      return
    }

    when (state){
      0 -> {
        if(compare(frame,CopleyMsg.resp_start_trace)) {
          state = 1
        }
      }

      1 -> {
        if(compare( frame, byteArrayOf(0x4b.toByte() ,0x01.toByte(), 0x25.toByte()) ) ){
          if(frame[4] == 0x02.toByte())
            state = 2
        }
      }

      2 -> {
        if(compare( frame, byteArrayOf(0xc2.toByte(),0x09.toByte(),0x25.toByte()) ) ){
          state = 3
        }
      }

      3 -> {
        var idx0 = frame[0]
        if(idx0 >0 ){
          if(this.idx +1 == idx0.toInt() ){
            this.idx = idx0.toInt()
            for(i in 1 until frame.size){
              temp_pos.add(frame[i])
              if(temp_pos.size == 4){
//                logger.info(station.buffer[3].toString()+"|"+station.buffer[2].toString()+"|"+station.buffer[1].toString()+"|"+station.buffer[0].toString())
                val value = Modbus.b2i(temp_pos[3],temp_pos[2],temp_pos[1],temp_pos[0])
                this.value.put(value.toDouble() / 5000)
                this.data.add(value.toDouble() / 5000)
                this.addPoint(value.toLong())
                temp_pos.clear()
              }
            }
          }
        }else{
          if(idx0 == 0xce.toByte()){
            state = 4
          }
        }
      }
      4-> {
        if(compare( frame, CopleyMsg.resp_last_upload ) ){
          state = 5
        }
      }
    }
  }

  fun addPoint(value:Long) {
    val size = positions.size
    if(size == 0) {
      base = System.currentTimeMillis() * 1000
    }

    positions.add(Point("position")
      .time(base + size  * interval , WritePrecision.US)
      .addField("pos",value))
  }

  fun  freq(freq: Int) {
    val l = Modbus.i2b(freq)
    this.freq = freq
    this.sendCommand(msg(0x2B,0x31,0x23,0x00 ,  l[3], l[2], l[1], l[0]))
  }

  fun amp(amp: Int) {
    val amp2 = (amp + amp_adjust).coerceAtMost(20000).coerceAtLeast(0)
    this.amp = amp
    val l = Modbus.i2b(amp2)
    this.sendCommand(msg(0x23,0x32,0x23,0x00 ,  l[3], l[2], l[1], l[0]))
  }

  fun stop_motor() {
    this.set_postion(0)
    this.prepare_to_stop()
  }

  fun jog(value: Int) {
    this.set_postion(value)
    this.prepare_to_move()
  }

  fun controller_temp() {
    this.sendCommand(msg(0x40,0x02,0x22,0x00,0x00,0x00,0x00,0x00))
  }

  fun motor_temp() {
    this.sendCommand(msg(0x40,0x90,0x21,0x00,0x00,0x00,0x00,0x00))
  }

  fun set_postion(value: Int) {
    val l = Modbus.i2b(value)
//    this.sendCommand(msg(0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x00))
    this.sendCommand(msg(0x2b,0x00,0x23,0x00,0x1e,0x00,0x00,0x00))
    this.sendCommand(msg(0x2f,0x60,0x60,0x00,0x01,0x00,0x00,0x00))
    this.sendCommand(msg(0x2b,0x86,0x60,0x00,0x00,0x00,0x00,0x00))
    this.sendCommand(msg(0x23,0x7a,0x60,0x00,l[3],l[2],l[1],l[0]))
//    this.sendCommand(msg(0x23,0x81,0x60,0x00,0x40,0x0D,0x03,0x00))
//    this.sendCommand(msg(0x23,0x83,0x60,0x00,0x40,0x0D,0x03,0x00))
//    this.sendCommand(msg(0x23,0x84,0x60,0x00,0x40,0x0D,0x03,0x00))
   }

  fun prepare_to_move() {
    this.sendCommand(msg(0x2B,0x40,0x60,0x00,0x4f,0x00,0x00,0x00))
    this.sendCommand(msg(0x2B,0x40,0x60,0x00,0x5f,0x00,0x00,0x00))
  }

  fun prepare_to_stop() {
    this.sendCommand(msg(0x2B,0x40,0x60,0x00,0x0f,0x00,0x00,0x00))
    this.sendCommand(msg(0x2B,0x40,0x60,0x00,0x1f,0x00,0x00,0x00))
  }



  fun start_motor() {
    this.freq(this.freq)
    this.amp(this.amp)
    this.position()
  }



  fun current(){
    this.sendCommand(msg(0x2B,0x00,0x23,0x00,0x04,0x00, 0x00,0x00))
  }
  fun position(){
    this.sendCommand(msg(0x2B,0x00,0x23,0x00,0x18,0x00, 0x00,0x00))
  }
  fun sine(){
    this.sendCommand(msg(0x2B,0x30,0x23,0x00,0x02,0x01, 0x00,0x00))
  }
  fun dutycycle(){
    this.sendCommand(msg(0x2B,0x33,0x23,0x00,0xE8,0x03, 0x00,0x00))
  }

  object CopleyMsg {
    val read_channel = arrayOf(
      msg(0x40, 0x00, 0x25, 0x00, 0x00, 0x00, 0x00, 0x00)
    )

    val trace_channel = arrayOf(
      msg(0x2B, 0x00, 0x25, 0x01, 0x1f, 0x00, 0x00, 0x00),
      msg(0x2B, 0x00, 0x25, 0x02, 0x00, 0x00, 0x00, 0x00),
      msg(0x2B, 0x00, 0x25, 0x03, 0x00, 0x00, 0x00, 0x00),
      msg(0x2B, 0x00, 0x25, 0x04, 0x00, 0x00, 0x00, 0x00),
      msg(0x2B, 0x00, 0x25, 0x05, 0x00, 0x00, 0x00, 0x00),
      msg(0x2B, 0x00, 0x25, 0x06, 0x00, 0x00, 0x00, 0x00)
    )


    val read_max_points = arrayOf(
      msg(0x40, 0x04, 0x25, 0x00, 0x00, 0x00, 0x00, 0x00)
    )

    val read_sample_cycle = arrayOf(
      msg(0x40, 0x02, 0x25, 0x00, 0x00, 0x00, 0x00, 0x00)
    )


    val set_sample_cycle = arrayOf(
      msg(0x2b, 0x05, 0x25, 0x00, 0x4a, 0x00, 0x00, 0x00)
    )

    val trigger_mode = arrayOf(
      msg(0x21, 0x06, 0x25, 0x00, 0x06, 0x00, 0x00, 0x00),
      msg(0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
    )

    val trigger_delay = arrayOf(
      msg(0x2b, 0x07, 0x25, 0x00, 0x00, 0x00, 0x00, 0x00)
    )

    val zero_pos = arrayOf(
      msg(0x23, 0x64, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00)
    )

    val start_trace = arrayOf(
      msg(0x2b, 0x08, 0x25, 0x00, 0x01, 0x00, 0x00, 0x00)
    )

    val stop_trace = arrayOf(
      msg(0x2b, 0x08, 0x25, 0x00, 0x00, 0x00, 0x00, 0x00)
    )

    val check_trace = arrayOf(
      msg(0x40, 0x01, 0x25, 0x00, 0x00, 0x00, 0x00, 0x00)
    )

    val start_upload = arrayOf(
      msg(0xa0, 0x09, 0x25, 0x00, 0x7f, 0x0e, 0x00, 0x00)
    )

    val first_query = arrayOf(
      msg(0xa3, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
    )

    val continue_query = arrayOf(
      msg(0xa2, 0x7f, 0x7f, 0x00, 0x00, 0x00, 0x00, 0x00)
    )

    val query: (Int) -> Array<ByteArray> = { i ->
      arrayOf(
        msg(0xa2, i, 0x7f, 0x00, 0x00, 0x00, 0x00, 0x00)
      )
    }


    val end_upload = arrayOf(
      msg(0xa1, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
    )


    val resp_start_trace = msg(0x60, 0x08, 0x25, 0x00, 0x00, 0x00, 0x00, 0x00)

    val resp_last_upload = msg(0xd9, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
    val resp_upload = msg(0xc2,0x09,0x25,0x00,0x00,0x10,0x00,0x00 )

  }



}
