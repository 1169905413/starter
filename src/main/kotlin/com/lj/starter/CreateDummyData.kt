package com.lj.starter

import com.influxdb.client.InfluxDBClient
import com.influxdb.client.InfluxDBClientFactory
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.write.Point
import com.lj.starter.hvdt.HTTP
import com.lj.starter.hvdt.INFLUX
import com.lj.starter.hvdt.STATUS
import io.vertx.core.Vertx
import io.vertx.kotlin.core.deployVerticleAwait
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.Query
import java.util.logging.Logger


fun main() {
//  val vertx = Vertx.vertx()
//
//  vertx.deployVerticle(INFLUX)
//
//
//
//  vertx.setTimer(1000){
//    STATUS.stations.forEach{it.initialCycle()}
//  }




////  InfluX
//  client = InfluxDBClientFactory.createV1(
//    db_url,
//    null,
//    "".toCharArray(),
//    database,
//    retentionPolicy
//  )
//
//
//

//
//  STATUS.stations.forEach{station->
////    println(it.id)
//    val queryString = "select cycle, cycle_AI, passCycle from one_hour.fpy where channel=${station.id} order by time desc limit 1"
//    val result = influxDB.query(Query(queryString))
//    result.results.forEach{
//      it.series.forEach{
//        it.values.forEach{
//          station.cycle = (it[1] as Double).toLong()
//          station.cycle_AI = (it[2] as Double).toLong()
//          station.passCycle = (it[3] as Double).toLong()
//        }
////        println(it.values[0][1])
//      }
//    }
//
//  }
//



//  for ( i in 0..5){
//    println(result)
//
//  }


INFLUX.init()
  val now = System.currentTimeMillis()
  for(  i in 0..10) {
    for (j in 0..5) {
      val point = Point.measurement("fpy").time( now + i*1000 + j,WritePrecision.MS)
        .addField("channel", j)
        .addField("cycle", 100 + i)
        .addField("cycle_AI", 100 + i)
        .addField("passCycle", 80 + i)
        .addField("freq", 0)
        .addField("amp", 0)
        .addField("max_delta", 0)
        .addField("min_delta", 0)
        .addField("threshold", 0)
        .addField("passDuration", 0)
        .addField("desireDuration", 0)
      INFLUX.write(point)
    }

  }
//

}
