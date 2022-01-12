package com.lj.starter.msft

import com.influxdb.client.InfluxDBClient
import com.influxdb.client.InfluxDBClientFactory
import com.influxdb.client.write.Point
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import java.util.logging.Logger

object INFLUX : AbstractVerticle() {

  var client: InfluxDBClient? = null
  const val database = "fata_test"
  const val retentionPolicy = "one_hour"
  const val db_url = "http://localhost:8086"

  val logger = Logger.getLogger(this.toString())

  override fun start(startPromise: Promise<Void>?) {
    logger.info("Deploy INFLUX!")
    client = InfluxDBClientFactory.createV1(
      db_url,
      null,
      "".toCharArray(),
      database,
      retentionPolicy
    )
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
