package com.lj.starter

class AvgVal() {
  var acc = 0.0
  var count = 0L

  fun put(v:Double) {
    acc += v
    count ++
  }

  fun avg() : Double{
    val value = if(count == 0L) 0.0 else acc / count
    acc = 0.0
    count = 0
    return value
  }



}
