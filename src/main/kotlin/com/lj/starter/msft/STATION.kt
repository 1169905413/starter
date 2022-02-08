package com.lj.starter.msft

data class STATION(val channel: Int) {
//  val offset = mutableListOf<Double>(0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0)
//  val offset2 = mutableListOf<Double>(0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0)
//  val compensation = mutableListOf<Double>(0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0)
//  val compensationBase = mutableListOf<Double>(0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0)
//  val slope = mutableListOf<Double>(200.0,200.0,200.0,200.0,200.0,200.0,200.0,200.0,200.0,200.0,200.0,200.0)
//  var pass = mutableListOf<Long>(0,0,0,0,0,0,0,0,0,0,0,0)
//  var pass_manuel = mutableListOf<Long>(0,0,0,0,0,0,0,0,0,0,0,0)
//  val threshold = mutableListOf<Double>(0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0)




  val offset = MutableList<Double>(channel) { _ -> 0.0 }
  val offset2 = MutableList<Double>(channel) { _ -> 0.0 }
  val offset_ai2 = MutableList<Double>(6) { _ -> 0.0 }
  val compensation = MutableList<Double>(channel) { _ -> 0.0 }
  val compensationBase = MutableList<Double>(channel) { _ -> 0.0 }
  val slope = MutableList<Double>(channel) { _ -> 200.0 }
  var pass = MutableList<Long>(channel) { _ -> 0 }
  var pass_manuel = MutableList<Long>(channel) { _ -> 0 }
  val threshold = MutableList<Double>(channel) { _ -> 0.0 }

}
