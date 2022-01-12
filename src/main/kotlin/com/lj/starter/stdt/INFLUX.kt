package com.lj.starter.stdt

import com.influxdb.client.InfluxDBClient
import com.influxdb.client.InfluxDBClientFactory
import com.influxdb.client.domain.Query
import com.influxdb.client.write.Point
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import java.util.logging.Logger

object INFLUX : AbstractVerticle() {

//  var client: InfluxDBClient? = null
  const val database = "hvdt_test"
  const val retentionPolicy = "one_hour"
  const val db_url = "http://localhost:8086"
  val logger = Logger.getLogger(this.toString())
  val    client = InfluxDBClientFactory.createV1(
    db_url,
    null,
    "".toCharArray(),
    database,
    retentionPolicy
  )
  override fun start(startPromise: Promise<Void>?) {
    logger.info("Deploy INFLUX!")
//    client = InfluxDBClientFactory.createV1(
//      db_url,
//      null,
//      "".toCharArray(),
//      database,
//      retentionPolicy
//    )
  }

  fun query_state(): MutableList<Long> {
    val queryApi = client!!.queryApi
//    val qq = Query().?
//    queryApi.query()
    val q = queryApi.query("select * cycle,passCycle_distance,passCycle_press from fpy order by time desc limit 1")
    val res = mutableListOf(0L,0L,0L)
    if(q.size > 0) {
      val rs = q.first()
      if(rs.records.size > 0 ) {
        res[0] = rs.records[0].getValueByKey("cycle").toString().toLong()
        res[1] = rs.records[0].getValueByKey("passCycle_distance").toString().toLong()
        res[2] = rs.records[0].getValueByKey("passCycle_press").toString().toLong()
      }
    }

    q.first().records[0].getValueByKey("")
    return res
  }

  fun write(pts:MutableList<Point>) {
    val writeApi = client!!.writeApi
    writeApi.writePoints(pts)
    writeApi.flush()
    writeApi.close()
  }

  fun write(pt:Point) {
    val writeApi = client!!.writeApi
    writeApi.writePoint(pt)
    writeApi.flush()
    writeApi.close()
  }

}
