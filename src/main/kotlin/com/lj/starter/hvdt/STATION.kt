package com.lj.starter.hvdt



import com.fasterxml.jackson.annotation.JsonIgnore
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import io.vertx.core.json.JsonArray
import org.influxdb.dto.Query

import java.util.logging.Logger
import kotlin.math.sin

data class STATION (
  val id:Int,
  var amp:Double = 0.0,
  var freq:Double = 1.0,
  var cycle : Long = 0,
  var cycle_AI : Long = 0,
  var start: Boolean = false,
  var maxCycle : Long = 1000000000L,
  var passCycle: Long = 0L,

  var autoControl:Boolean = false,
  var threshold: Double = 100.0,
  var desireDuration:Double = 5.0,
  var initAdjRate:Double = 0.25,
  var controlAdjRate:Double = 0.005,
  var maxDuration:Double = 7.0,
  var minDuration:Double = 5.5,
  var updateControl:Long = 2,

  var targetTemp:Double = 0.0,


  var checkAOCycle:Boolean = true,
  var checkAICycle:Boolean = true
//  var rate:Double=1.0
)
{
  private val logger: Logger = Logger.getLogger(this.toString())

  val id_card: Int = id % AI.station
  val channel_base = id_card * AI.channel_per_station
  val amp_coeff = 1000

  var freq_range = 0.1..25.0
  var amp_range = 0.0..10.0
  var temp_range = 0.0..100.0
  var amp_current_range = 0.0..1.5
  var cycle_range = 1L..9999999999L
  var duration_range = 0.0..100.0
  var control_range = 1L..100L
  var rate_range = 0.001..2.0
  var phase: Double = 0.0
  var progress: Double = 0.0
  var passCurrent: Double = 0.0
  var d:Double = 0.0
  var p:Double = 0.0
  var d_offset:Double = 0.0
  var p_offset:Double = 0.0
  var passDuration = 0.0

  var max_delta:Double = 0.0
  var min_delta:Double = 0.0



  var max_p:Double = 0.0
  var min_p:Double = 0.0

  var max_d:Double = 0.0
  var min_d:Double = 0.0


  var max_delta_current:Double = -Double.MAX_VALUE
  var min_delta_current:Double = Double.MAX_VALUE

  var max_p_current:Double = -Double.MAX_VALUE
  var min_p_current:Double = Double.MAX_VALUE

  var max_d_current:Double = -Double.MAX_VALUE
  var min_d_current:Double = Double.MAX_VALUE

  var rate0 = 1.35
  var rate1 = 1.36
  var rate2 = 1.23
  var rate3 = 1.3
  var rate4 = 1.35
  var rate5 = 1.16
  var temp:Double = 0.0
  var ctemp:Double = 0.0
  var state = 0
  var idx = 0
  var base = 0L
  val interval = 50L

  @JsonIgnore
  val positions = arrayListOf<Point>()

  @JsonIgnore
  val buffer = arrayListOf<Byte>()

//  var can_status = 0

   fun updateamp() {
      if (autoControl&&start) {
        if (passDuration < minDuration) {
          amp = updateIfValid(amp,amp+controlAdjRate,"amp",amp_current_range)
//          CAN2.bei_amplitude(this,(amp*amp_coeff).toInt());
        } else if(passDuration > maxDuration){
          amp = updateIfValid(amp,amp-controlAdjRate,"amp",amp_current_range)
//          CAN2.bei_amplitude(this,(amp*amp_coeff).toInt());
        }
      }
  }

  fun addPoint(value:Long) {
    val size = positions.size
    if(size == 0) {
      base = System.currentTimeMillis() * 1000
    }

    positions.add(Point("position")
      .time(base + size  * interval , WritePrecision.US)
      .addField("pos",value))
  }

  fun genWave(buffer: DoubleArray, chan: Int){
    val unit = freq / AO.clock
    for (t in 0 until AO.section_len){
      var value = 0.0
      if(start) {
        value = amp * sin(phase * AO.twoPi)
        phase += unit
      }
      buffer[t * AO.channel +  chan] = value
    }
    val cyclePeriod = phase.toLong()
    phase -= cyclePeriod
    cycle = if(start) cycle + cyclePeriod else cycle
  }

  fun updatePressure(data : DoubleArray, time:Int):STATION{

    d =  AI.getdata(data,time, channel_base) + d_offset
    p =  AI.getdata(data,time, channel_base + 1) + p_offset

    return this
  }

  fun delta() =  d - p
  fun pass() = delta() > threshold
  fun one_cycle() = AI.round / freq

  fun savePoint(point: Point):STATION{
    point.addField("p$id_card", p)
    point.addField("d$id_card", d)
    point.addField("delta$id_card", delta())
    return this
  }

  fun update(values:JsonArray){
    for ( i in 0 until values.size()){
      val kv = values.getJsonObject(i)
      when ( val k = kv.getString("key")) {
        "amp" -> {
          amp = updateIfValid(amp, kv.getDouble("value"), k, amp_current_range)
          if(start) {
//            CAN2.bei_amplitude(this, (amp*amp_coeff).toInt())
//            CAN2.bei_freq(this, (freq).toInt())
//            CAN2.bei_update(this)
            CAN2.motor.freq((freq * 100).toInt(), this)
            CAN2.motor.amp((amp * 100).toInt(), this)

          } else{
//            CAN2.amplitude(this,0);
          }
        }

        "freq" -> {
          freq = updateIfValid(freq, kv.getDouble("value"), k, freq_range)
          if(start) {
            CAN2.motor.freq((freq * 100).toInt(), this)
            CAN2.motor.amp((amp * 100).toInt(), this)
//            CAN2.bei_amplitude(this, (amp*amp_coeff).toInt())
//            CAN2.bei_freq(this, (freq).toInt())
//            CAN2.bei_update(this)
          } else{
//            CAN2.amplitude(this,0);
          }
        }

        "cycle" -> cycle = updateIfValid(cycle, kv.getLong("value"), k, cycle_range)
        "cycle_AI" -> cycle_AI = updateIfValid(cycle_AI,kv.getLong("value"),k, cycle_range)
        "maxCycle" -> maxCycle = updateIfValid(maxCycle,kv.getLong("value"),k, cycle_range)
        "passCycle" -> passCycle = updateIfValid(passCycle,kv.getLong("value"),k, cycle_range)

        "start" -> {
          start = kv.getBoolean("value")
          if(start) {
//            CAN2.bei_amplitude(this, (amp*amp_coeff).toInt())
//            CAN2.bei_freq(this, (freq).toInt())
//            CAN2.bei_start(this)
//            CAN2.motor.amp((amp*100).toInt(),this);
//            CAN2.motor.freq((freq*100).toInt(),this);
//            CAN2.motor.start_motor(this)
            CAN2.motor.freq((freq*100).toInt(),this)
            CAN2.motor.amp((amp*100).toInt(),this)
            CAN2.motor.current(this)
          } else{
            CAN2.motor.stop_motor(this)
//            CAN2.bei_stop(this)
//            CAN2.bei_update(this)
          }
        }

        "autoControl" -> autoControl = kv.getBoolean("value")
        "threshold" -> threshold = kv.getDouble("value")
        "desireDuration" -> desireDuration = updateIfValid(desireDuration,kv.getDouble("value"),k, duration_range)

        "updateControl" -> updateControl = updateIfValid(updateControl,kv.getLong("value"),k, control_range)
        "initAdjRate" -> initAdjRate= updateIfValid(initAdjRate,kv.getDouble("value"),k, rate_range)
        "controlAdjRate" -> controlAdjRate = updateIfValid(controlAdjRate,kv.getDouble("value"),k, rate_range)
        "maxDuration" -> maxDuration = updateIfValid(maxDuration,kv.getDouble("value"),k, duration_range)
        "minDuration" -> minDuration = updateIfValid(minDuration,kv.getDouble("value"),k, duration_range)

        "checkAOCycle" -> checkAOCycle = kv.getBoolean("value")
        "checkAICycle" -> checkAICycle = kv.getBoolean("value")

        "temp" -> temp = kv.getDouble("value")
        "d_offset" -> d_offset = kv.getDouble("value")
        "p_offset" -> p_offset = kv.getDouble("value")

        "targetTemp" -> {
          targetTemp = updateIfValid(targetTemp,kv.getDouble("value"),k, temp_range)
          CAN2.setTemp(id,(targetTemp*10).toInt())
        }
      }
    }
  }

  fun initialCycle(){
    val queryString = "select cycle, cycle_AI, passCycle from one_hour.fpy where channel=${id} order by time desc limit 1"
    val result = INFLUX.queryClient!!.query(Query(queryString))

    result.results?.forEach{
      it.series?.forEach{
        it.values?.forEach{
          println((it[1] as Double).toLong())
          cycle = (it[1] as Double).toLong()
          cycle_AI = (it[2] as Double).toLong()
          passCycle = (it[3] as Double).toLong()
        }
      }
    }
  }



  private fun updateIfValid(oldV:Double, newV:Double, key:String, range: ClosedFloatingPointRange<Double>) =
    if(newV in range)  newV
    else {
      logger.info("${key} value $newV out of range")
      oldV
    }

  private fun updateIfValid(oldV:Long, newV:Long, key:String, range: LongRange) =
    if(newV in range)  newV
    else {
          logger.info("$key value $newV out of range")
          oldV
    }


  fun calculate(): Point? {
    if (start) {
      val total = one_cycle()
      val remain = total - progress
      max_delta_current = max_delta_current.coerceAtLeast(delta())
      min_delta_current = min_delta_current.coerceAtMost(delta())

      max_p_current = max_p_current.coerceAtLeast(p)
      min_p_current = min_p_current.coerceAtMost(p)

      max_d_current = max_d_current.coerceAtLeast(d)
      min_d_current = min_d_current.coerceAtMost(d)

      val pass = pass()

      if (remain > AI.unit) {
        progress += AI.unit
        if(pass) passCurrent += AI.unit
      } else {
        cycle_AI++
        cycle = cycle_AI
        passDuration =( (passCurrent + if(pass) remain else 0.0 ) / total ) * 100
        if(passDuration > desireDuration) passCycle++
        progress = AI.unit - remain
        passCurrent = if(pass) progress else 0.0

        max_delta = max_delta_current
        min_delta = min_delta_current

        max_p = max_p_current
        min_p = min_p_current

        max_d = max_d_current
        min_d = min_d_current

        max_delta_current = -Double.MAX_VALUE
        min_delta_current = Double.MAX_VALUE
        max_p_current = -Double.MAX_VALUE
        min_p_current = Double.MAX_VALUE
        max_d_current = -Double.MAX_VALUE
        min_d_current = Double.MAX_VALUE


        return Point.measurement("fpy")
          .addField("channel", id)
          .addField("cycle", cycle)
          .addField("cycle_AI", cycle_AI)
          .addField("passCycle", passCycle)
          .addField("freq", freq)
          .addField("amp", amp)
          .addField("max_delta", max_delta)
          .addField("min_delta", min_delta)
          .addField("threshold", threshold)
          .addField("passDuration", passDuration)
          .addField("desireDuration", desireDuration)
      }
    }
    return null
  }

}

//class State{
//  var trace = false
//  var complete = false
//
//  fun toInt() =if(complete) 2 else 0 + if(trace) 1 else 0
//
//}





//val AO_amps = mutableListOf<Double>(1.0,0.0,0.0,0.0,0.0,0.0)
//val AO_freqs = mutableListOf<Double>(10.0,10.0,10.0,0.0,0.0,0.0)
//val AO_phases = mutableListOf<Double>(0.0,0.0,0.0,0.0,0.0,0.0)
//val AO_cycle = mutableListOf<Long>(0,0,0,0,0,0)
//val AO_CHANNEL_START = mutableListOf<Boolean>(true,true,false,false,false,false)




