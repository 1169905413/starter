package com.lj.starter.msft

import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import com.lj.starter.msft_can_ai.CACHE
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.util.logging.Logger

object DEVICE : AbstractVerticle() {
  private val logger: Logger = Logger.getLogger(this.toString())

//  val offset = mutableListOf<Double>(0.0,0.0,0.0,0.0,0.0,0.0)
//  val offset2 = mutableListOf<Double>(0.0,0.0,0.0,0.0,0.0,0.0)
//  val compensation = mutableListOf<Double>(0.0,0.0,0.0,0.0,0.0,0.0)
//  val compensationBase = mutableListOf<Double>(0.0,0.0,0.0,0.0,0.0,0.0)
//  val slope = mutableListOf<Double>(200.0,200.0,200.0,200.0,200.0,200.0)
//  val pass = mutableListOf<Long>(0,0,0,0,0,0)
//  val pass_manuel = mutableListOf<Long>(0,0,0,0,0,0)
//  val threshold = mutableListOf<Double>(0.0,0.0,0.0,0.0,0.0,0.0)
  private val channels = MSFT.channel
  val stations = STATION(channels)
  val starthistory = mutableListOf<Triple<Long,Long,Long>>()
  var cycle = 0L
  var preCycle = 0L
  var cycle_manuel = 0L
  var freq = 15.0
  var pool = mutableListOf<MutableList<Double>>()
  var autoCompensate = false


  var startpoint = 0L
  var count = false

  fun status (): JsonObject {
    return JsonObject()
      .put("threshold", stations.threshold)
      .put("cycle", adjustCycle())
      .put("pass",getAdjustPass())
      .put("frequency",freq)
      .put("offset",stations.offset)
      .put("offset2",stations.offset2)
      .put("compensation",stations.compensation)
      .put("compensationBase",stations.compensationBase)
      .put("autoCompensate",autoCompensate)
      .put("startpoint",startpoint)
      .put("slope",stations.slope)
      .put("history",starthistory)
      .put("count",count)
  }

  fun adjust(raw:Double, c:Int) = (raw - stations.offset[c] + (if (!autoCompensate) 0.0 else stations.compensation[c]))/stations.slope[c]


  fun adjustCycle() = preCycle + cycle + cycle_manuel
  fun adjustPass(c:Int) = stations.pass[c] + stations.pass_manuel[c]
  fun getAdjustPass(): MutableList<Long> {
    val l = MutableList<Long>(channels) { _ -> 0L }
    for (i in 0 until l.size) {
      l[i] = adjustPass(i)
    }
    return l
  }

  fun <T> process(data: T, f: (T, Int, Int) -> Double) {
    val base = (System.currentTimeMillis() - 30) * 1000
    val list = mutableListOf<Point>()
    val cache = JsonArray()
    for(i in 0 until 60) {
//      println(i)
//      continue
      val point = Point.measurement("raw").time(base + i  * 500 , WritePrecision.US)
      val buffer = mutableListOf<Double>()
      val cachePoint = JsonArray()

      for( c in 0 until channels) {

        val raw =  f(data,i,c)
        var value = adjust(raw,c)
        buffer.add(raw)
        cachePoint.add(value)
        point.addField("ai$c", value)
      }

      cache.add(cachePoint)
      pool.add(buffer)
      list.add(point)


      if(pool.isNotEmpty() && pool.size % 2000 == 0) {
        val now = System.currentTimeMillis()
//        println(now)
        if(cycle >= freq*(now - startpoint + 1000) /1000) {
          logger.severe("discard cycle for next 1s:$cycle")
        }else {
          stations.offset2.fill(0.0)
          val fpy = Point.measurement("fpy").addField("cycle", adjustCycle()  )
          val step = (2000/ freq).toInt()
          val peak = MutableList<Int>(channels) { _ -> -step }
//            mutableListOf<Int>(-step,-step,-step,-step,-step,-step)

          if(count){
            cycle += (freq * (pool.size / 2000).toInt()).toInt()
          }

          for (id in 0 until  pool.size) {
            val l = pool[id]
            for( c in 0 until channels) {
              stations.offset2[c] += l[c]
              if(count){
                val value = adjust(l[c],c)
                if(value >= stations.threshold[c] && id - peak[c] > step){
                  stations.pass[c]++
                  peak[c] = id
                }
              }
            }
          }

          for(i in 0 until channels) {
            stations.offset2[i] = stations.offset2[i] / pool.size
            stations.compensation[i] = stations.compensationBase[i] - stations.offset2[i]
            if(count){
              fpy.addField("ai$i", adjustPass(i))
            }
          }
          logger.fine("update compensation:${stations.compensation.joinToString ()}")
          if(count) {
            INFLUX.write(fpy)
          }

        }

        pool.clear()
      }
    }
    CACHE.add(cache)
    INFLUX.write(list)
  }


  override fun start(startPromise: Promise<Void>?) {
    logger.info("Deploy Device!")
  }

  fun resetoffset(id :Int, value: Double, auto:Boolean) : JsonArray {
    if(auto) {
      stations.offset[id] = stations.offset2[id]
    } else {
      stations.offset[id] = value
    }
    return JsonArray(stations.offset)
  }

  fun addPass(id :Int, value: Long) : JsonArray {
    stations.pass_manuel[id] = value
    return JsonArray(stations.pass_manuel)
  }

  fun addCycle( value: Long)  {
    cycle_manuel = value
  }


  fun resetslope(id :Int, value: Double) : JsonArray {
    stations.slope[id] = value
    return JsonArray(stations.slope)
  }


  fun resetthreshold(id :Int, value: Double) : JsonArray {
    stations.threshold[id] = value
    return JsonArray(stations.threshold)
  }

  fun setCompensation(b:Boolean) {
    autoCompensate = b
  }

  fun setCompensationValue(){
    for (i in 0 until channels) {
      stations.compensationBase[i] = stations.offset2[i]
    }
  }

  fun update(p:String) {
    if(p == "i r0 4"){
      count = true
      val now = System.currentTimeMillis()
      startpoint = now - 1000L
    }

    if(p == "i r0 1") {
      count = false
      preCycle+=cycle
      starthistory.add(Triple(startpoint,System.currentTimeMillis(), cycle))
      cycle = 0
      startpoint = 0
    }

    if(p.startsWith("i r17 ")){
      freq = p.split(" ")[2].toDouble()
    }
  }

}
