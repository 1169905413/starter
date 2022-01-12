package com.lj.starter.hvdt


import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import io.vertx.core.json.JsonObject

object STATUS {
  val stations = mutableListOf<STATION>(
    STATION(0),STATION(1),STATION(2),STATION(3),STATION(4),STATION(5),STATION(6),STATION(7)
  )

  fun status (): JsonObject {
    try{
      return JsonObject()
        .put("stations", stations)
    }catch (e:Exception){
      e.printStackTrace()
    }
    return JsonObject()
  }

  fun update(data: DoubleArray, start:Long, base:Int, measurement:String) {
    val list = mutableListOf<Point>()
    for(i in 0 until AI.section_len) {
      val point = Point.measurement(measurement).time(start + i  * AI.unit , WritePrecision.US)
      for ( j in 0 until AI.station) {
        val station = stations[j+base]
        val fpy = station
          .updatePressure(data,i)
          .savePoint(point)
          .calculate()
        list.add(point)
        if(fpy != null) INFLUX.write(fpy)
      }
    }
    INFLUX.write(list)
  }

}
