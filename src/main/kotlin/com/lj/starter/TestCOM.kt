package com.lj.starter

import com.lj.starter.msft.COM
import io.vertx.core.Vertx
import jssc.SerialPort
import jssc.SerialPortEvent
import jssc.SerialPortEventListener
import jssc.SerialPortList
import okhttp3.internal.toHexString
import okio.ByteString.Companion.decodeHex

val port = SerialPort("COM4")
fun main() {


  println("0F".decodeHex()[0].toByte())
//  return

  SerialPortList.getPortNames().forEach { println(it) }
  port.openPort()
  port.addEventListener (listener())

  port.setParams(              SerialPort.BAUDRATE_9600,
SerialPort.DATABITS_8,
  SerialPort.STOPBITS_1,
  SerialPort.PARITY_NONE)
  Vertx.vertx()
//  port.openPort()
  port.writeBytes("1234".toByteArray())
//  port.closePort()

////  port.rea
//  println(port.flowControlMode)
}

class listener():SerialPortEventListener{
  override fun serialEvent(p0: SerialPortEvent?) {
    var s = ""
//    println(p0)
    val bytes =     port.readBytes()
    bytes.forEach {       print(it.toByte().toInt().toHexString()+" ") }
    println()

    val data  = "01 03 1A 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 00 02 00 02 00 02 00 14 31 6D".split(" ").map{it.decodeHex()[0].toByte()}

    port.writeBytes(
              byteArrayOf(
        0x01,0x01,0x03, 0x1B.toByte(),0x02, 0x10.toByte(),0x4d,0x85.toByte()
              )
    )
    port.writeBytes(
//      data.toByteArray()
      bytes
//      byteArrayOf(
//        0x11,0x01,0x05, 0xCD.toByte(),0x6B, 0xB2.toByte(),0x0E,0x1B,0x45,0xe6.toByte()
//      )
//        byteArrayOf(
//        0x01,0x01,0x03, 0x1B.toByte(),0x02, 0x10.toByte(),0x4d,0x85.toByte()

//    byteArrayOf(0x11, 0x03 , 0x12 ,
//      0x00 ,0xDC.toByte() , 0x00 , 0xDC.toByte() , 0x00.toByte() , 0xDC.toByte(),
//      0x00 ,0xDC.toByte() , 0x00 , 0xDC.toByte() , 0x00.toByte() , 0xDC.toByte(),
//      0x00 ,0xDC.toByte() , 0x00 , 0xDC.toByte() , 0x00.toByte() , 0xDC.toByte(),
//        0xd9.toByte(),0xc6.toByte())
    )
//    do {
//
//      var b = port.readBytes(1)[0]
//
//
//    } while (b != 13.toByte())
//    println("nb")
  }

}
