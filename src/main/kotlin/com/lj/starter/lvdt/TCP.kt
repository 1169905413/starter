package com.lj.starter.lvdt

import com.influxdb.client.write.Point
import com.lj.starter.Modbus

import com.lj.starter.Modbus.READ_AI

import io.vertx.core.AbstractVerticle
import io.vertx.core.net.NetClient
import io.vertx.core.net.NetSocket
import io.vertx.kotlin.core.net.connectAwait
import java.util.logging.Logger

object TCP :AbstractVerticle() {
  val logger = Logger.getLogger(this.toString())
  var deployID:String? = null
  var client : NetClient? = null
  var cs: NetSocket? = null
  val offset = mutableListOf<Double> (0.0, -50.0)
  val slope = mutableListOf<Double> (0.00496, 0.02)
  val channel = mutableListOf<Int> (6,7)
  val title = mutableListOf<String> ("displacement","temperature")
  var failed = 0
  override fun start() {
      client = vertx.createNetClient()
  }

    fun connect(ip:String = "192.168.1.30", port:Int = 502) {
      client!!.connect(port, ip) { res ->
        if (res.succeeded()) {
          logger.info("TCP Connected!")
          failed = 0
          var socket = res.result()
          cs = socket
          socket.handler { buffer ->
            val bytes = buffer.bytes
            if (Modbus.action(bytes) == READ_AI) {
              val point = Point.measurement("raw")
              for (i in 0 until title.size){
                val value =  Modbus.value(bytes, channel[i]) * slope[i] + offset[i]
                println(value)
                point.addField(title[i], value)
              }
              INFLUX.write(point)
            }
          }
        }
        else {
          logger.severe("Failed to connect: ${res.cause()}")
          }
        }
    }


  fun startTimer (time:Long) {
    if(deployID == null)
      vertx.deployVerticle(Timer(time)){
        deployID = it.result()
        logger.info("Timer Deployed:$time ms and id: $deployID")
      }
    else{
      logger.severe("Timer already Deployed id: $deployID")
    }
  }


  fun stopTimer () {
    if(deployID != null){
      vertx.undeploy(deployID){
        deployID = null
      }
    }
  }

  class Timer(val time : Long) : AbstractVerticle () {
    var count = 0
    override fun start() {
      vertx.setPeriodic(time) {
        if(cs != null){
          cs!!.write(Modbus.request(count, READ_AI,64 ,8)){
           if(it.failed()){
             failed += 1
//             client!!.close()
             connect()
           }
          }
          count = if(count == Int.MAX_VALUE) 0 else count +1
        }else {
          logger.severe("TCP NOT Connected!")
        }
      }
    }
  }

}
