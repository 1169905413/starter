package com.lj.starter.stdt

import Automation.BDaq.ValueRange


object AO {
  const val twoPi = 2 * Math.PI
  const val station = 1
  const val channel_per_station = 4
  const val channel =  station * channel_per_station
  const val section_len = 200
  const val step = 200L
  const val clock = section_len * 1000/step .toDouble()
  const val buffer_len = section_len * channel
  val vr: ValueRange = ValueRange.V_Neg10To10
}


object AI{
  const val unit = 200
  const val station = 1
  const val channel_per_station = 1
  const val channel = station * channel_per_station

  const val section_len = 5000
  const val round = 1000000
  const val clock:Double = (round/unit).toDouble()
  const val buffer_len = section_len * channel
  var vr: ValueRange = ValueRange.V_Neg10To10

  var slope = 75
  var offset = 0.0

  val getdata:(DoubleArray, Int , Int)->Double = {d, i ,c ->
   ( d[i * channel + c]-5)*slope + offset
  }
}
