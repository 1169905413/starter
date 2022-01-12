package com.lj.starter.stdt



import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.influxdb.client.write.Point
import com.lj.starter.Val
import io.vertx.core.json.JsonArray

import java.util.logging.Logger
import kotlin.math.abs

data class STATION (
  val id:Int,
  var amp:Double = 0.0,
  var amp2:Double = 0.0,
  var freq:Double = 1.0,
  var cycle : Long = 0,
  var cycle_AI : Long = 0,
  var start: Boolean = false,
  var maxCycle : Long = 1000000000L,
  var passCycle: Long = 0L,

  var passCycle_press: Long = 0L,
  var passCycle_distance: Long = 0L,
  var autoControl:Boolean = false,
  var threshold: Double = 100.0,
  var desireDuration:Double = 5.0,
  var initAdjRate:Double = 0.25,
  var controlAdjRate:Double = 0.005,
//  var maxDuration:Double = 7.0,
//  var minDuration:Double = 5.5,
  var updateControl:Long = 2,
  var targetTemp:Double = 0.0,
  var checkAOCycle:Boolean = true,
  var checkAICycle:Boolean = true
)
{
  private val logger: Logger = Logger.getLogger(this.toString())

  @JsonIgnore
  val AI_CARD_ID: Int = id % AI.station
  @JsonIgnore
  val AI_CHANNEL_BASE = AI_CARD_ID * AI.channel_per_station
  @JsonIgnore
  val AO_CARD_ID: Int = id % AO.station
  @JsonIgnore
  val AO_CHANNEL_BASE = AO_CARD_ID * AO.channel_per_station
  @JsonIgnore
  var freq_range = 0.1..50.0
  @JsonIgnore
  var amp_range = 0.0..20.0
  @JsonIgnore
  var temp_range = 0.0..100.0
//  @JsonIgnore
//  var amp_current_range = 0.0..2.0
  @JsonIgnore
  var cycle_range = 0L..9999999999L
  @JsonIgnore
  var duration_range = 0.0..100.0
  @JsonIgnore
  var control_range = 1L..100L
  @JsonIgnore
  var rate_range = 0.001..2.0
//  @JsonIgnore
//  var phase: Double = 0.0
  @JsonIgnore
  var progress: Double = 0.0

  var d:Double = 0.0

  var distance = 0.0
  var distance_base = 1.0
  var distance_max = 0.0

  var distance_ori = 0.0
  var distance_base_ori = 1.0
  var distance_max_ori = 0.0

  var distance_ratio_0 = 1.0
  var distance_avg_N1 = 0.0

  var distance_thresh = 1.01
  var mode = true

  var press_up = 100.0
  var press_down = 0.0
  var press_range = 5.0

  var press_control = 0.0f

  var left_motor=""
  var right_motor =""
  var left_amplifier = ""
  var right_amplifier = ""

  var max_d = 0.0
  var min_d = 0.0
  var delta = 0.0

  var adjust = false
  var adjust2 = false
  var saved = false
  var delta_base = 0.0
  var maxPressLimit = 200.0
  var avg_n_1 = 0.0
  var ratio = 1.0

//  @JsonGetter("delta_auto")
//  fun delta() = max_d - min_d

  @JsonIgnore
  val d_val = Val()
  @JsonIgnore
  val d_origin = Val()
//  val d_zero = Val()
//  val d_N_Neg_1 = Val()

  var temp:Double = 0.0

  fun updatePressure(data : DoubleArray, time:Int):STATION{
    val ori_d =  AI.getdata(data,time, AI_CHANNEL_BASE)

    if(ori_d > maxPressLimit){
      STATUS.com.stop_motor()
      start = false
      this.logger.severe("Exceed Max Pressure limit: $ori_d")
    }

    d = ori_d
    if(adjust) {
      d = d * ratio + avg_n_1
    }
    d_origin.put(ori_d)
    d_val.put(d)
    return this
  }

  fun one_cycle() = AI.round / freq

  fun savePoint(point: Point):STATION{
    point.addField("press", d)
    return this
  }

  fun update(values:JsonArray){
    for ( i in 0 until values.size()){
      val kv = values.getJsonObject(i)
      when ( val k = kv.getString("key")) {
        "amp" -> {
          amp = updateIfValid(amp, kv.getDouble("value"), k, amp_range)
          amp2 = amp
          if(start) {
            STATUS.com.set_param2(freq,amp,amp2)
          }
        }

        "amp2" -> {
          amp2 = updateIfValid(amp2, kv.getDouble("value"), k, amp_range)
          if(start) {
            STATUS.com.set_param2(freq,amp,amp2)
          }
        }

        "freq" -> {
          freq = updateIfValid(freq, kv.getDouble("value"), k, freq_range)
          if(start) {
            STATUS.com.set_param2(freq,amp,amp2)
          }
        }

        "cycle" -> cycle = updateIfValid(cycle, kv.getLong("value"), k, cycle_range)
        "cycle_AI" -> cycle_AI = updateIfValid(cycle_AI,kv.getLong("value"),k, cycle_range)
        "maxCycle" -> maxCycle = updateIfValid(maxCycle,kv.getLong("value"),k, cycle_range)
        "passCycle" -> passCycle = updateIfValid(passCycle,kv.getLong("value"),k, cycle_range)

        "distance_thresh" -> distance_thresh = kv.getDouble("value")
        "mode" -> mode = kv.getBoolean("value")
        "press_Control" -> {
          val value = kv.getDouble("value")
          STATUS.com.setPressure(id,value.toFloat())
        }

        "passCycle_press" -> passCycle_press = updateIfValid(passCycle_press, kv.getLong("value"), k, cycle_range)
        "passCycle_distance" -> passCycle_distance = updateIfValid(passCycle_distance,kv.getLong("value"),k, cycle_range)

        "press_up" -> press_up = kv.getDouble("value")
        "press_down" -> press_down = kv.getDouble("value")
        "press_range" -> press_range = kv.getDouble("value")

        "home" -> STATUS.com.home()
        "motor_on" -> STATUS.com.motor_on()
        "motor_off" -> STATUS.com.motor_off()

        "start" -> {
          start = kv.getBoolean("value")
          if(start) {
            STATUS.com.set_param2(freq,amp,amp2)
          } else {

            STATUS.com.stop_motor()
          }
        }



        "autoControl" -> autoControl = kv.getBoolean("value")
        "threshold" -> threshold = kv.getDouble("value")
        "desireDuration" -> desireDuration = updateIfValid(desireDuration,kv.getDouble("value"),k, duration_range)

        "updateControl" -> updateControl = updateIfValid(updateControl,kv.getLong("value"),k, control_range)
//        "initAdjRate" -> initAdjRate= updateIfValid(initAdjRate,kv.getDouble("value"),k, rate_range)
//        "controlAdjRate" -> controlAdjRate = updateIfValid(controlAdjRate,kv.getDouble("value"),k, rate_range)
//        "maxDuration" -> maxDuration = updateIfValid(maxDuration,kv.getDouble("value"),k, duration_range)
//        "minDuration" -> minDuration = updateIfValid(minDuration,kv.getDouble("value"),k, duration_range)

        "checkAOCycle" -> checkAOCycle = kv.getBoolean("value")
        "checkAICycle" -> checkAICycle = kv.getBoolean("value")
        "adjust" ->{
          adjust = kv.getBoolean("value")
        }
        "adjust2" ->{
          adjust2 = kv.getBoolean("value")
        }
        "saved" ->{
          saved = kv.getBoolean("value")
        }


        "targetTemp" -> {
          targetTemp = updateIfValid(targetTemp,kv.getDouble("value"),k, temp_range)
          STATUS.com.setTemp(id,(targetTemp*10).toInt())
        }
      }
    }
  }



//  private fun updateIfValid(oldV:Double, newV:Double, key:String, range: ClosedFloatingPointRange<Double>) =
//    if(newV in range)  newV
//    else {
//      logger.info("${key} value $newV out of range")
//      oldV
//    }
//
//  private fun updateIfValid(oldV:Long, newV:Long, key:String, range: LongRange) =
//    if(newV in range)  newV
//    else {
//          logger.info("$key value $newV out of range")
//          oldV
//    }

  private fun <T: Comparable<T>> updateIfValid(oldV:T, newV:T, key:String, range: ClosedRange<T>) =
    if(newV in range)  newV
    else {
      logger.info("$key value $newV out of range")
      oldV
    }


  fun calculate(): Point? {
    if (start) {
      val remain = one_cycle() - progress
      if (remain > AI.unit) {
        progress += AI.unit
      } else {
        cycle_AI++
        cycle = cycle_AI
        progress = AI.unit - remain
        d_val.now()
        d_origin.now()

        max_d = d_val.max
        min_d = d_val.min
        delta = max_d - min_d

        distance = distance_ori
        if(!adjust2) {
          distance_base = distance_base_ori
          distance_max = distance_max_ori
        } else {
//          val avg = (distance_max_ori + distance_base_ori) / 2
//          val r = distance_max_ori / distance_base_ori
//          val d = (distance_ratio_0 - 1 ) / (distance_ratio_0+1) * distance_avg_N1
          val d = distance_max_ori * (distance_max_ori -distance_base_ori) /(3*distance_max_ori + distance_base_ori)
          distance_base = distance_base_ori - d
          distance_max = distance_max_ori + d
        }



        distance_avg_N1 =  (distance_max_ori + distance_base_ori) / 2

        if(!adjust && !saved){
          delta_base = delta
//          distance_ratio_0 = distance_max_ori / distance_base_ori
        }



        val d_n_1 = d_origin.max - d_origin.min
        ratio = if(d_n_1 == 0.0) 1.0 else delta_base / d_n_1
        avg_n_1 = d_origin.avg * ( 1-ratio)


        if(distance_max/distance_base >= 1 + distance_thresh / 100) passCycle_distance++
        if(abs(max_d - press_up) <= press_range &&  abs(min_d - press_down) <= press_range) passCycle_press++
        passCycle = if(mode) passCycle_distance else passCycle_press

        if(cycle >= maxCycle) {
          start = false
          STATUS.com.stop_motor()
        }

        return   Point.measurement("fpy")
          .addField("channel", id)
          .addField("cycle", cycle)
          .addField("cycle_AI", cycle_AI)
          .addField("passCycle", passCycle)
          .addField("freq", freq)
          .addField("amp", amp)
          .addField("mode", mode)
          .addField("passCycle_press", passCycle_press)
          .addField("passCycle_distance", passCycle_distance)
          .addField("max_d", max_d)
          .addField("min_d", min_d)
          .addField("max_distance", distance_base)
          .addField("min_distance", distance_max)
      }

    }
    return null
  }



//  fun genWave(buffer: DoubleArray){
//    val unit = freq / AO.clock
//    for (t in 0 until AO.section_len){
//      var value = 0.0
//      if(start) {
//        value = amp * sin(phase * AO.twoPi)
//        phase += unit
//      }
//      for( i  in 0 until AO.channel_per_station){
//        buffer[t * AO.channel + AO_CHANNEL_BASE +  i] = value
//      }
//    }
//    val cyclePeriod = phase.toLong()
//    phase -= cyclePeriod
//    cycle = if(start) cycle + cyclePeriod else cycle
//  }

}






