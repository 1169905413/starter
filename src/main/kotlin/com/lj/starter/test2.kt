package com.lj.starter

import IntArrayByReference
import com.lj.starter.maps.MAPS
import com.lj.starter.stdt.STDT
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import java.time.LocalDateTime

fun main() {
//  T().start()
  for (i in 0..3){
    println(i)
  }

//  Vertx.vertx().fileSystem().readFile("C:\\Users\\jiali\\project\\55.txt"){
//    it.result().toString().split(" ").forEach{
//      if(it == "FB" || it == "FC") {
//        println()
//      }
//      print("$it ")
//    }
//  }
//  Vertx.vertx().close()
  vertx.deployVerticle(MAPS)
}
val vertx = Vertx.vertx()

class T{
  val result = mutableMapOf<String,String>()
  var id = "NA"

//  fun connect(){
//    R(this).start()
//  }
  fun start(){
//    connect()
  var count = 1
  val distance = "L1,+35.57215,GO"
  println(distance.split(",")[1])
    vertx.setPeriodic(1000000) {
      when {
        count++ == 10 -> {
         println("mode"+"SD,ME,1,01")
          println("start_distance_max"+"U1")

          //        count = 0
        }

        count > 10 -> {
          println("distance_max"+"L1,1")
          println("mode"+"SD,ME,1,00")
          count = 0
        }

        else -> {
          println("distance"+"M1,1")
        }
      }
//      println(result.toString())
    }
  }
}

class R(val t:T){
  fun start(){
    vertx.setPeriodic(1000){
      t.result["ANC"]="123"
    }
  }

}
