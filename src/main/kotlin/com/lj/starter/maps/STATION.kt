package com.lj.starter.maps

import com.fasterxml.jackson.annotation.JsonIgnore
import com.lj.starter.vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.util.logging.Logger
import kotlin.math.atan

data class STATION(
  var acc:Double = 50000.0,
  var bend:Double = 0.0,
  var ext:Double = 0.0,
  var rot:Double = 0.0,
  var vmax1:Double = 0.0,
  var vmax2:Double = 0.0,
  var meter:Long = 0,
  var temp:Double = 0.0,
  var amp:Double = 0.0,
  var freq:Double = 1.0,
  var start:Boolean = false
){
  private val logger: Logger = Logger.getLogger(this.toString())
  @JsonIgnore
  val data=
    arrayListOf(
      arrayListOf(),
      arrayListOf(),
      arrayListOf<Int>()
    )
  @JsonIgnore
  var freq_range = 0.1..60.0
  @JsonIgnore
  var amp_range = 0.0..20.0

  var count_base = 0
  var act_pos=    arrayListOf(0.0,0.0,0.0 )
  var act_direction=    arrayListOf(true, true, true)
  var act_count = arrayListOf(0,0,0)
  var count = 0L
  var com_K= 0
  var com_k1= 0
  var com_k2= 0
  var com_R= 0
  var offsete = 0.0
  var offsetb = 0.0
  var offsetr = 0.0
  var agito_num=0.0
  var a = 0
  fun get_count():Long{
    return count_base + (act_count.max())!!.toLong()/2
  }

  private fun <T: Comparable<T>> updateIfValid(oldV:T, newV:T, key:String, range: ClosedRange<T>) =
    if(newV in range)  newV
    else {
      logger.info("$key value $newV out of range")
      oldV
    }
//  fun calcute(pipe:Double,num:Double):MutableList<MutableList<Double>>{
//    var list1 = mutableListOf<Double>()
//    var list2 = mutableListOf<Double>()
//    var list3 = mutableListOf<Double>()
//    var list4 = mutableListOf<MutableList<Double>>()
////    var pipe = 287.0
////    var ppue = 32.0
////    var ppub = 50
////    var ppur = 1.0
//    println(com_K)
//    println(com_k)
//      for(j in 0 until data[0].size){
//        var K = 185.25+20*(com_K*(5/4))+offsetb
//        var k = 285.0+20*(com_k*(5/4))+offsete
//        var R = ((data[2][j]+com_R)/5000.0)*360.0
//        var tention = 0.0
////    var t = pipe+20
//        var t = pipe+num
//        var lbate = Math.acos((76429.0 - Math.pow(k, 2.0))/(200*Math.sqrt(66429.0)))- Math.acos((76429.0 - Math.pow(k + (data[1][j]/1000.0*20.0), 2.0))/(200*Math.sqrt(66429.0)))
//        var l3 = Math.acos(t / 350)
//        var l4 = l3 + lbate
//        var e = 350 * Math.cos(l4)
//        var ppu = e*(data[1][j]/1000.0*20.0)
//        var percent_e = (t / e - 1)*100
//        var detalb = Math.sqrt((Math.pow((K + (data[0][j]/1000.0*20.0)), 2.0)) - (Math.pow(217 - t/ 2, 2.0))) - Math.sqrt(Math.pow(K, 2.0) - (Math.pow( 217- e/2 , 2.0)))
//        var lb = 2 * atan((2 * detalb) / e)
//        list1.add(String.format("%.2f",lb*180/Math.PI).toDouble())
//        list2.add(String.format("%.2f",percent_e).toDouble())
//        list3.add(String.format("%.2f",R).toDouble())
//      }
//    list4.add(list1)
//    list4.add(list2)
//    list4.add(list3)
//    return list4
//  }

//  fun low_pass_ao(input: MutableList<MutableList<Double>>): MutableList<MutableList<Double>> {
//    for(i in 1 until input[0].size){
//      input[0][i] = input[0][i-1] * 0.1 + input[0][i] * 0.9
//      input[1][i] = input[1][i-1] * 0.1 + input[1][i] * 0.9
//      input[2][i] = input[2][i-1] * 0.1 + input[2][i] * 0.9
//      println(i)
//    }
//    return input
//  }

  fun get_agito(){
    if(a==1){
      vertx.setPeriodic(1000){
        if(a==0){
          vertx.cancelTimer(it)
        }
        println(111)
        STATUS.station.agito_num+=freq
      }
    }
  }
  fun update(values: JsonArray){
    for ( i in 0 until values.size()){
      val kv = values.getJsonObject(i)
      when ( val k = kv.getString("key")) {
        "amp" -> {
          amp = updateIfValid(amp, kv.getDouble("value"), k, amp_range)
//          amp2 = amp
          if(start) {
//            STATUS.com.set_param(freq,amp)
            Agito.set_param(freq,amp)
          }
        }
        "freq" -> {
          freq = updateIfValid(freq, kv.getDouble("value"), k, freq_range)
          if(start) {
//            STATUS.com.set_param(freq,amp)
            Agito.set_param(freq,amp)
          }
        }
        "home" -> STATUS.com.home()
        "motor_on" -> STATUS.com.motor_on()
        "motor_off" -> STATUS.com.motor_off()
        "start" -> {
          start = kv.getBoolean("value")
          if(start) {
            try{
              a=1
              get_agito()
              Agito.set_param(freq,amp)
              Agito.start_motor()
            }catch (e:Exception){
              e.printStackTrace()
            }
          } else {
            a=0
            get_agito()
            Agito.stop_motor()
          }
        }

      }
    }
  }
}
