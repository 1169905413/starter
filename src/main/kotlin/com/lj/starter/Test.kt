package com.lj.starter

import com.lj.starter.msft_can_ai.CopleyMotor
import com.lj.starter.msft_can_ai.MSFT
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject


fun main() {
//  val init = CANBridge2.Init(CAN2.timeout,1,2, intArrayOf(500,1000))
//  println(init)

  val l = Modbus.i2b((14.1*100).toInt())
  val a = intArrayOf(1,2,3,4)

    vertx.deployVerticle(MSFT)

  for (i in 0 until a.size){
    println(i)
  }

  println(Modbus.b2i((-1).toByte().toInt(), (-1).toByte().toInt(), (-3).toByte().toInt(), (-118).toByte().toInt()))
  println(Modbus.b2i((-1).toByte(), (-1).toByte(), (-3).toByte(), (-118).toByte()))
println(Modbus.i2b(-200))
  println((-1).toByte())
  println(Modbus.b2i(0xff, 0xff, 0xfe, 0x73))

  println(0xCE.toInt() < 0)

  println(0xCE)
  println(0xCE.toByte())
  println(0xCE.toByte().toInt())
  println(JsonObject().put("data", mutableListOf(1,2,3)))
println(-1 and 0x0000ffff)
  var d1 = mutableListOf(1,2,3)
  var d2 = d1.toList()
  println(d2)
  d1.clear()
  println(d2)

  val m = CopleyMotor(0,0)
//  println(m)
  println(JsonObject().put("motor", m.toJson()).toString())
}

