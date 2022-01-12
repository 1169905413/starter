package com.lj.starter.stdt


import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import io.vertx.core.json.JsonObject

object STATUS {
  val stations = mutableListOf<STATION>(
    STATION(0)
  )

  val com = COM_ALL(COM("COM1"),COM("COM10"), null)
//  val ao = PCI_AO("ao#1",0)
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
        if(fpy != null) INFLUX.write(fpy)
        list.add(point)
      }
    }
    INFLUX.write(list)
  }

}
