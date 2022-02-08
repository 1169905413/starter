package com.lj.starter.hvdt

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
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import java.util.logging.Logger

import com.lj.starter.Modbus.msg
import com.sun.org.apache.xpath.internal.operations.Mod

object CAN2: AbstractVerticle() {
  private val logger: Logger = Logger.getLogger(this.toString())
  val step = 1L
  val timeout = 50
  val temp_channel = 1
  val moter_channel = 0
  val motor = CopleyMotor(moter_channel)

  override fun start(startPromise: Promise<Void>?) {
    logger.info("Deploy CAN")


    val init = CANBridge2.Init(timeout,1,2, intArrayOf(500,1000))
    logger.info("init = $init")
    if(init) {
      readtemp()
      vertx.deployVerticle(motor)
    } else{
      logger.severe("Failed to deploy CAN")
    }

  }



  fun bei_amplitude(station:STATION ,amp:Int){
    val l = Modbus.i2b(amp)

//    val hbyte = amp and 0xff00 shr 8
//    val lbyte = amp and 0x00ff
    val id = station.id
    val cob = id + 0x601
    CANBridge2.Send(id, cob, 8 ,  msg(0x22,0x00,0x2f,0x03 ,l[3], l[2], l[1], l[0]),moter_channel)
  }


  fun bei_start(station:STATION){
    val id = station.id
    val cob = id + 0x601
    CANBridge2.Send(id, cob, 8 ,  msg(0x22,0x00,0x2f,0x01 , 0x01,0x00,0x00,0x00),moter_channel)
  }



  fun bei_stop(station:STATION){
    val id = station.id
    val cob = id + 0x601
    CANBridge2.Send(id, cob, 8 ,  msg(0x22,0x00,0x2f,0x01 , 0x00,0x00,0x00,0x00),moter_channel)
  }

  fun bei_update(station:STATION){
    val id = station.id
    val cob = id + 0x601
    CANBridge2.Send(id, cob, 8 ,  msg(0x23,0x46,0x31,0x00 , 0x00,0x00,0x00,0x00),moter_channel)
  }

  fun bei_enable(station:STATION){
    val id = station.id
    val cob = id + 0x601
    CANBridge2.Send(id, cob, 8 ,  msg(0x23,0x46,0x31,0x00 , 0x01,0x00,0x00,0x00),moter_channel)
  }


//  fun bei_disable(station:STATION){
//    val id = station.id
//    val cob = id + 0x601
//    CANBridge2.Send(id, cob, 8 ,  msg(0x23,0x46,0x31,0x00 , 0x00,0x00,0x00,0x00),moter_channel)
//  }


  fun bei_freq(station:STATION,freq:Int){
    val l = Modbus.i2b(freq)
//    val hbyte = freq and 0xff00 shr 8
//    val lbyte = freq and 0x00ff
    val id = station.id
    val cob = id + 0x601
    CANBridge2.Send(id, cob, 8,  msg(0x22,0x00,0x2f,0x04 , l[3], l[2], l[1], l[0]),moter_channel)
  }





  fun setTemp(id:Int, temp:Int) {
    val idH = Modbus.hbyte(id)
    val idL = Modbus.lbyte(id)
    val tempH = Modbus.hbyte(temp)
    val tempL = Modbus.lbyte(temp)

    val woCRC = byteArrayOf(0,6, idH,idL , tempH, tempL,0,0)
    val CRC = CRC16()
    CRC.update(woCRC,0,6)
    val crc = CRC.value.toInt()
    val crcH = Modbus.hbyte(crc)
    val crcL = Modbus.lbyte(crc)
    woCRC[6] = crcL
    woCRC[7] = crcH
    CANBridge2.Send(0,0, 8,  woCRC, temp_channel)
  }

  fun readtemp() {
    val len = 256;
    val buffer = ByteByReference(len * 8)
    val num = IntByReference(0)
    val lengths = IntArrayByReference(len)
    val ids = IntArrayByReference(len)
    val cobs = IntArrayByReference(len)

    val temp = byteArrayOf(0,0,0,0,0,0,0,0,0,0,0,0)
    var idx = 0
    val head = 3
    val data = 12
    val tail = 2

    vertx.setPeriodic(step) {
      val ret = CANBridge2.Read(len,num,lengths,cobs,ids, buffer,temp_channel)
      if(num.value == 0 || ret != 0){
        logger.fine("No message. Code :$ret")
      } else{
//        println(idx)
        for(i in 0 until num.value){
          val l = lengths.getValue(i.toLong())
          if(l == 8){
            if(b2v(buffer,i,0)==0x00.toByte()
              && b2v(buffer,i,1)==0x04.toByte()
              && b2v(buffer,i,2)==0x0c.toByte()
            ){
              idx = 0
            }
          }

          for (j in 0 until l) {

            if(idx >= head && idx < head + data){
              temp[idx - head] = b2v(buffer,i,j)
            }

            idx++
            if(idx >= head + data + tail ){
              idx = 0
            }
          }
        }

        if(idx == 0){
          STATUS.stations.forEach{
            it.temp = String.format("%.2f",(Modbus.b2i(temp[it.id * 2] , temp[it.id * 2 + 1]).toDouble() / 10)*1.08).toDouble()
          }
//          STATUS.stations[0].temp = (Modbus.b2i(temp[STATUS.stations[0].id * 2] , temp[STATUS.stations[0].id * 2 + 1]).toDouble() / 10)*STATUS.stations[0].rate0
//          STATUS.stations[0].temp = String.format("%.2f",STATUS.stations[0].temp).toDouble()
//          STATUS.stations[1].temp = (Modbus.b2i(temp[STATUS.stations[1].id * 2] , temp[STATUS.stations[1].id * 2 + 1]).toDouble() / 10)*STATUS.stations[1].rate1
//          STATUS.stations[1].temp = String.format("%.2f",STATUS.stations[1].temp).toDouble()
//          STATUS.stations[2].temp = (Modbus.b2i(temp[STATUS.stations[2].id * 2] , temp[STATUS.stations[2].id * 2 + 1]).toDouble() / 10)*STATUS.stations[2].rate2
//          STATUS.stations[2].temp = String.format("%.2f",STATUS.stations[2].temp).toDouble()
//          STATUS.stations[3].temp = (Modbus.b2i(temp[STATUS.stations[3].id * 2] , temp[STATUS.stations[3].id * 2 + 1]).toDouble() / 10)*STATUS.stations[3].rate3
//          STATUS.stations[3].temp = String.format("%.2f",STATUS.stations[3].temp).toDouble()
//          STATUS.stations[4].temp = (Modbus.b2i(temp[STATUS.stations[4].id * 2] , temp[STATUS.stations[4].id * 2 + 1]).toDouble() / 10)*STATUS.stations[4].rate4
//          STATUS.stations[4].temp = String.format("%.2f",STATUS.stations[4].temp).toDouble()
//          STATUS.stations[5].temp = (Modbus.b2i(temp[STATUS.stations[5].id * 2] , temp[STATUS.stations[5].id * 2 + 1]).toDouble() / 10)*STATUS.stations[5].rate5
//          STATUS.stations[5].temp = String.format("%.2f",STATUS.stations[5].temp).toDouble()

          STATUS.stations.forEach{
//            it.temp = (Modbus.b2i(temp[it.id * 2] , temp[it.id * 2 + 1]).toDouble() / 10)*it.rate
            it.ctemp=String.format("%.2f",it.temp/STATUS.stations.size).toDouble()
          }
        }
      }
    }
    vertx.setPeriodic(2000) {
      CANBridge2.Send(0,0, 8,  msg(0,4,0,0,0,6, 0x71, 0xD9),temp_channel)
    }
  }

  fun b2v(buffer:ByteByReference , i:Int , j:Int) = (buffer.getValue((i * 8 + j).toLong()).toInt() and 0xff).toByte()

}

abstract class Motor(val chan:Int):AbstractVerticle(){

  val step = 5L
  val len = 256;
  val buffer = ByteByReference(len * 8)
  val num = IntByReference(0)
  val lengths = IntArrayByReference(len)
  val ids = IntArrayByReference(len)
  val cobs = IntArrayByReference(len)
  val logger: Logger = Logger.getLogger(this.toString())

  val send_cob = 0x601
  val get_cob = 0x581

  override fun start() {
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
            frame[j] =  CAN2.b2v(buffer, i, j)
          }
          val new_cob = id - get_cob

          if(new_cob >=0 && new_cob < STATUS.stations.size){
            this.read_frame(frame,STATUS.stations[new_cob])
          }else{
            logger.severe("wrong cob id: $cob")
          }
        }
      }
    }
    super.start()
  }



  fun sendCommands(msgs:Array<ByteArray>,station: STATION) {
    val id = station.id
    val cob = id + send_cob
    msgs.forEach {
        msg->  CANBridge2.Send(id, cob, 8,  msg, chan)
    }
  }

  fun sendCommand(msg:ByteArray,station: STATION) {
    val id = station.id
    val cob = id + send_cob
    CANBridge2.Send(id, cob, 8,  msg, chan)
  }

  fun compare(frame: ByteArray, head:ByteArray):Boolean {
    for(i in 0 until minOf(frame.size,head.size)){
      if(head[i] != frame[i]) return false
    }
    return true
  }

  abstract fun freq(freq:Int,station: STATION)
  abstract fun amp(amp:Int,station: STATION)
  abstract fun start_motor(station: STATION)
  abstract fun stop_motor(station: STATION)
  abstract fun read_frame( frame:ByteArray,station:STATION)

}


class CopleyMotor(chan: Int) : Motor(chan) {

  override fun start() {
    super.start()

    STATUS.stations.forEach {station->
      position(station)
      sine(station)
      dutycycle(station)

      this.sendCommands(CopleyMsg.trace_channel,station)
      this.sendCommands(CopleyMsg.trigger_mode,station)
      this.sendCommands(CopleyMsg.trigger_delay,station)
//      this.sendCommands(CopleyMsg.zero_pos,station)
    }

    vertx.setPeriodic(50L) {
      val station = STATUS.stations[0]

      when (station.state){
        0 -> {
          this.sendCommands(CopleyMsg.start_trace,station)
        }

        1 -> {
          this.sendCommands(CopleyMsg.check_trace,station)
        }

        2 -> {
          this.sendCommands(CopleyMsg.start_upload,station)
        }

        3 -> {
          if(station.positions.size == 0){
            this.sendCommands(CopleyMsg.first_query,station)
          } else if (station.idx == 0x7f) {
            station.idx = 0
            this.sendCommands(CopleyMsg.continue_query,station)
          }
        }

        4-> {
          this.sendCommands(CopleyMsg.query(station.idx + 1),station)
        }


        5-> {
          INFLUX.write(station.positions)
          station.buffer.clear()
          station.positions.clear()
          station.idx = 0
          this.sendCommands(CopleyMsg.end_upload,station)
          station.state = 0
        }
      }
    }
  }



  override fun read_frame(frame: ByteArray, station: STATION) {
    logger.fine("frame:" + frame.joinToString { "," })

    when (station.state){
      0 -> {
        if(compare(frame,CopleyMsg.resp_start_trace)) {
          station.state = 1
        }
      }

      1 -> {
        if(compare( frame, byteArrayOf(0x4b.toByte() ,0x01.toByte(), 0x25.toByte()) ) ){
          if(frame[4] == 0x02.toByte())
            station.state = 2
        }
      }

      2 -> {
        if(compare( frame, byteArrayOf(0xc2.toByte(),0x09.toByte(),0x25.toByte()) ) ){
          station.state = 3
        }
      }

      3 -> {
        var idx = frame[0]
        if(idx >0 ){
          if(station.idx +1 == idx.toInt() ){
            station.idx = idx.toInt()
            for(i in 1 until frame.size){
              station.buffer.add(frame[i])

              if(station.buffer.size == 4){
//                logger.info(station.buffer[3].toString()+"|"+station.buffer[2].toString()+"|"+station.buffer[1].toString()+"|"+station.buffer[0].toString())
                val value = Modbus.b2i(station.buffer[3],station.buffer[2],station.buffer[1],station.buffer[0])
                station.addPoint(value.toLong())
                station.buffer.clear()
              }
            }
          }
        }else{
          station.state = 4
        }
      }

      4-> {
        if(compare( frame, CopleyMsg.resp_last_upload ) ){
          station.state = 5
        }
      }
    }

  }



  override fun freq(freq: Int,station: STATION) {
    val l = Modbus.i2b(freq)
    this.sendCommand(msg(0x2B,0x31,0x23,0x00 ,  l[3], l[2], l[1], l[0]),station)
  }

  override fun amp(amp: Int,station: STATION) {
    val l = Modbus.i2b(amp)
    this.sendCommand(msg(0x23,0x32,0x23,0x00 ,  l[3], l[2], l[1], l[0]),station)
  }

  override fun stop_motor(station: STATION) {
    val l = Modbus.i2b(0)
    this.sendCommand(msg(0x23,0x32,0x23,0x00 ,  l[3], l[2], l[1], l[0]),station)
  }


  override fun start_motor(station: STATION) {
    TODO("Not yet implemented")
  }




  fun current(station: STATION){
    this.sendCommand(msg(0x2B,0x00,0x23,0x00,0x04,0x00, 0x00,0x00),station)
  }

  fun position(station: STATION){
    this.sendCommand(msg(0x2B,0x00,0x23,0x00,0x18,0x00, 0x00,0x00),station)
  }
  fun sine(station: STATION){
    this.sendCommand(msg(0x2B,0x30,0x23,0x00,0x02,0x01, 0x00,0x00),station)
  }

  fun dutycycle(station: STATION){
    this.sendCommand(msg(0x2B,0x33,0x23,0x00,0xE8,0x03, 0x00,0x00),station)
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








//  fun setchannel(station:STATION) {
//    val id = station.id
//    val cob = id + 0x601
//    CANBridge2.Send(id, cob, 8,  msg(0x2B,0x00,0x25,0x01 , 0x1f, 0x00, 0x00, 0x00),moter_channel)
//    CANBridge2.Send(id, cob, 8,  msg(0x2B,0x00,0x25,0x02 , 0x00, 0x00, 0x00, 0x00),moter_channel)
//    CANBridge2.Send(id, cob, 8,  msg(0x2B,0x00,0x25,0x03 , 0x00, 0x00, 0x00, 0x00),moter_channel)
//    CANBridge2.Send(id, cob, 8,  msg(0x2B,0x00,0x25,0x04 , 0x00, 0x00, 0x00, 0x00),moter_channel)
//    CANBridge2.Send(id, cob, 8,  msg(0x2B,0x00,0x25,0x05 , 0x00, 0x00, 0x00, 0x00),moter_channel)
//    CANBridge2.Send(id, cob, 8,  msg(0x2B,0x00,0x25,0x06 , 0x00, 0x00, 0x00, 0x00),moter_channel)
//  }
//
//
//  fun setTraceCycle(station:STATION) {
//    val id = station.id
//    val cob = id + 0x601
//    CANBridge2.Send(id, cob, 8,  msg(0x2B,0x05,0x25,0x00 , 0x4a, 0x00, 0x00, 0x00),moter_channel)
//  }
//
//  fun trigger(station:STATION) {
//    val id = station.id
//    val cob = id + 0x601
//    CANBridge2.Send(id, cob, 8,  msg(0x21,0x06,0x25,0x00 , 0x06, 0x00, 0x00, 0x00),moter_channel)
//    CANBridge2.Send(id, cob, 8,  msg(0x21,0x03,0x00,0x00 , 0x00, 0x00, 0x00, 0x00),moter_channel)
//    CANBridge2.Send(id, cob, 8,  msg(0x2b,0x07,0x25,0x00 , 0x00, 0x00, 0x00, 0x00),moter_channel)
//  }
//
//  fun trace(station:STATION) {
//    val id = station.id
//    val cob = id + 0x601
//    CANBridge2.Send(id, cob, 8,  msg(0x2b,0x08,0x25,0x00 , 0x01, 0x00, 0x00, 0x00),moter_channel)
//  }
//
//  fun checkTrace(station:STATION) {
//    val id = station.id
//    val cob = id + 0x601
////    vertx
//    CANBridge2.Send(id, cob, 8,  msg(0x40,0x01,0x25,0x00 , 0x00, 0x00, 0x00, 0x00),moter_channel)
//  }
//
//  fun upload(station:STATION){
//    val id = station.id
//    val cob = id + 0x601
//    CANBridge2.Send(id, cob, 8,  msg(0xa0,0x09,0x25,0x00 , 0x7f, 0x0e, 0x00, 0x00),moter_channel)
//    CANBridge2.Send(id, cob, 8,  msg(0xa3,0x00,0x00,0x00 , 0x00, 0x00, 0x00, 0x00),moter_channel)
//  }
//
//  fun continue_upload(station:STATION,code:Int){
//    val id = station.id
//    val cob = id + 0x601
//    CANBridge2.Send(id, cob, 8,  msg(0xa2,code,0x7f,0x00 , 0x00, 0x00, 0x00, 0x00),moter_channel)
//  }
//
//  fun close_upload(station:STATION) {
//    val id = station.id
//    val cob = id + 0x601
//    CANBridge2.Send(id, cob, 8,  msg(0xa1,0x00,0x00,0x00 , 0x00, 0x00, 0x00, 0x00),moter_channel)
//    station.can_status = 0
//  }
