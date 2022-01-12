package com.lj.starter.hvdt

import com.influxdb.client.InfluxDBClient
import com.influxdb.client.InfluxDBClientFactory
import com.influxdb.client.write.Point
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import java.util.logging.Logger

object INFLUX {

  var client: InfluxDBClient? = null
  const val database = "hvdt_test"
  const val retentionPolicy = "one_hour"
  const val db_url = "http://localhost:8086"

  var queryClient :InfluxDB ? = null
  val logger = Logger.getLogger(this.toString())

  fun init() {
    logger.info("Deploy INFLUX!")
    client = InfluxDBClientFactory.createV1(
      db_url,
      null,
      "".toCharArray(),
      database,
      retentionPolicy
    )
    queryClient = InfluxDBFactory.connect(db_url, "test", "");

    queryClient!!.setDatabase(database)
    logger.info("END Deployment")

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
