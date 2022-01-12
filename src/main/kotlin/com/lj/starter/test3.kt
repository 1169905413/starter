package com.lj.starter

import Automation.BDaq.BufferedAoCtrl
import Automation.BDaq.DeviceInformation
import Automation.BDaq.InstantAoCtrl
import Automation.BDaq.InstantDoCtrl
import java.lang.Exception


//import com.sun.
fun main() {
  try{
    val devInfo = DeviceInformation("do#1")
//  val instantDoCtrl = InstantDoCtrl()

    val bao = BufferedAoCtrl()  //<-- buffered AO
    val iao = InstantAoCtrl()   //<-- instant AO
    val ido = InstantDoCtrl()   //<-- buffered DO

    bao.selectedDevice = devInfo
    bao.Prepare()
    iao.selectedDevice = devInfo
    ido.selectedDevice = devInfo

//    bao.Prepare()
//    bao2.Prepare()

    ido.Write(0,0)
    ido.Dispose()
  }catch (e:Exception){
    e.printStackTrace()
  }

//  Vertx.vertx().deployVerticle(MSFT)
//  val s = STATION(0)
//  println(JsonObject().put("nb",mutableListOf(s)))
//
//  s.d_val.put(1.0)
//  s.d_val.put(2.0)
//  s.d_val.put(-1.0)
//
//  s.d_val.now()
//  s.max_d = s.d_val.max
//  s.min_d = s.d_val.min
//  println(JsonObject().put("nb",mutableListOf(s)))
//
//
//
//  val woCRC = byteArrayOf(1,0x10,0,0x6a,0,2,4,0,0,0x41,0xb0.toByte())
//  val CRC = CRC16()
//  CRC.update(woCRC,0,woCRC.size)
//  val crc = CRC.value.toInt()
//  val crcH = Modbus.hbyte(crc)
//  val crcL = Modbus.lbyte(crc)
//println(0xD9.toByte())
//println(crcL)
//  val i = 11.0.toFloat().toBits()
//  println(Modbus.i2b(i))
////  println(11.0.toFloat().toBits())
////  s.d_val.put(2.0)
////  s.d_val.max()
////  s.d_val.min()
////  println(JsonObject().put("nb",mutableListOf(s)))
////
////  s.d_val.put(-1.0)
////  s.d_val.max()
////  s.d_val.min()
////  println(JsonObject().put("nb",mutableListOf(s)))
//
//  print(36.toChar())
//  print(48.toChar())
//  print(49.toChar())
//  print(54.toChar())
//  println("NB" + '\r'.toInt())
//
//
//  arrayOf(35,48,49,48,48,48,49,13).forEach { print(it.toChar()) }
}

