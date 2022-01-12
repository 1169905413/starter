package com.lj.starter.hvdt

import Automation.BDaq.ValueRange
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import io.vertx.core.json.JsonArray

object AO {
  const val twoPi = 2 * Math.PI
  val channel =  4
  val section_len = 200
  val step = 200L
  val clock = section_len * 1000/step .toDouble()
  val buffer_len = section_len * channel
  val vr: ValueRange = ValueRange.V_Neg10To10
}


object AI{
  val unit = 200
  val station = 4
  val channel_per_station = 2

  val section_len = 5000
  val round = 1000000
  val clock:Double = (round/unit).toDouble()
  val channel = station * channel_per_station
  val buffer_len = section_len * channel
  var vr: ValueRange = ValueRange.V_Neg10To10

  var slope = 100/3
  var offset = 0.0

  val getdata:(DoubleArray, Int , Int)->Double = {d, i ,c ->
    d[i * channel + c]*slope + offset
  }
}
