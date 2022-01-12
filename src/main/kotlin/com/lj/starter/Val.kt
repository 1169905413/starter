package com.lj.starter

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.logging.Logger

class Val() {
  private val logger: Logger = Logger.getLogger(this.toString())
  private fun init(max:Boolean) = if(max) -Double.MAX_VALUE else Double.MAX_VALUE
  @JsonIgnore
  var max_val = init(true)
  @JsonIgnore
  var min_val = init(false)
  @JsonIgnore
  var acc_val = 0.0
  var rw = true
  var acc = 0.0
  var count = 0
  var max = 0.0
  var min = 0.0
  var avg = 0.0

  @JsonGetter
  fun delta() = max - min


  fun close() {
    rw = false
  }

  fun open() {
    rw = true
  }

  fun put(v:Double) {
    if(rw){
      max_val = max_val.coerceAtLeast(v)
      min_val = min_val.coerceAtMost(v)
      count ++
      acc_val += v
    } else{
      logger.severe("Update to a closed value!")
    }

  }

  fun now() {
    if(rw) {
      max = max_val
      min = min_val
      acc = acc_val
      avg = if(count == 0) 0.0 else acc / count

      max_val = init(true)
      min_val = init(false)
      count = 0
      acc_val = 0.0
    }else{
      logger.severe("Save to an closed value!")
    }

  }
//  fun get(): Pair<Double, Double> {
//    return max() to min()
//  }

//  fun max():Double {
//    max = max_val
//    max_val = init(true)
//    return max
//  }
//
//  fun min():Double {
//    min = min_val
//    min_val = init(false)
//    return min
//  }
}
