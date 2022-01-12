package com.lj.starter



import com.lj.starter.hvdt.HVDT
import com.lj.starter.maps.COM
import com.lj.starter.maps.MAPS
import com.lj.starter.maps.STATUS
import io.vertx.core.*
import io.vertx.core.json.JsonObject
import jssc.SerialPort
import java.util.logging.Logger
import kotlin.math.atan

class MainVerticle : AbstractVerticle() {
  val logger = Logger.getLogger(this.toString())

  override fun start(startPromise: Promise<Void>) {
//    vertx.deployVerticle(CAN)
//    vertx.deployVerticle(MSFT)
//    vertx.deployVerticle(HVDT)
//    vertx.deployVerticle(LVDT)
//    vertx.deployVerticle(STDT)
    vertx.deployVerticle(MAPS)
//    var offsete = 0.0
//    var offsetb = 0.0
//    var offsetr = 0.0
//
//    var com = SerialPort("COM7")
//    com.openPort()
//    vertx.setPeriodic(1000){
//      com.writeString("ac\r")
//      var s = com.readString().toString()
//      println(s.substring(11))
//    }




//    var list1 = mutableListOf<Double>()
//    var list2 = mutableListOf<Double>()
//    var list3 = mutableListOf<Double>()
//    var list4 = mutableListOf<MutableList<Double>>()
//    var K = 185.25+20*(0*(5/4))+offsetb
//    var k = 285.0+20*(0*(5/4))+offsete
////    var R = ((data[2][j]+comr)/5000)*360.0
//    var tention = 0.0
////    var t = pipe+20
//    var t = 287+20
//    var lbate = Math.acos((76429.0 - Math.pow(k, 2.0))/(200*Math.sqrt(66429.0)))- Math.acos((76429.0 - Math.pow(k - (1600/1000*20), 2.0))/(200*Math.sqrt(66429.0)))
//    var l3 = Math.acos(t / 350.0)
//    var l4 = l3 + lbate
//    var e = 350 * Math.cos(l4)
////    var ppu = e*data[1][j]
//    var percent_e = (t / e - 1)*100
//    var detalb = Math.sqrt((Math.pow((K + 2500/1000*20), 2.0)) - (Math.pow(217.0 - t/ 2, 2.0))) - Math.sqrt(Math.pow(K, 2.0) - (Math.pow( 217- e/2 , 2.0)))
//    var lb = 2 * atan((2 * detalb) / e)
//    println(detalb)
//    list1.add(String.format("%.2f",lb*180/Math.PI).toDouble())
//    list2.add(String.format("%.2f",percent_e).toDouble())
//    println(lb*180/Math.PI)
//    println(percent_e)
//    list3.add(String.format("%.2f",R).toDouble())
//    var list1 = mutableListOf<Double>()
//    var list2 = mutableListOf<Double>()
//    var list3 = mutableListOf<Double>()
//    var list4 = mutableListOf<MutableList<Double>>()
//    vertx.setPeriodic(5000){
//      list4.add(list1)
//      list4.add(list2)
//      list4.add(list3)
//      list1=mutableListOf<Double>()
//      list2=mutableListOf<Double>()
//      list3=mutableListOf<Double>()
//      println(list4)
//    }

//    vertx.setPeriodic(10){
//      var K = 185.25+20*(STATUS.station.com_K*(5/4))+ STATUS.station.offsetb
//      var k = 285.0+20*(STATUS.station.com_k*(5/4))+ STATUS.station.offsete
//      var R = ((AdvMotBridge.AdvGetActPos(2)+ STATUS.station.com_R)/5000.0)*360.0
//      var tention = 0.0
////    var t = pipe+20
//      var t = pipe+num
//      var lbate = Math.acos(
//        (76429.0 - Math.pow(
//          k,
//          2.0
//        )) / (200 * Math.sqrt(66429.0))
//      ) - Math.acos(
//        (76429.0 - Math.pow(k + (AdvMotBridge.AdvGetActPos(1) / 1000.0 * 20.0), 2.0)) / (200 * Math.sqrt(
//          66429.0
//        ))
//      )
//      var l3 = Math.acos(t / 350.0)
//      var l4 = l3 + lbate
//      var e = 350 * Math.cos(l4)
//      var ppu = e*(AdvMotBridge.AdvGetActPos(1)/1000.0*20.0)
//      var percent_e = (Math.cos(l3) / Math.cos(l4) - 1)*100
//      var detalb = Math.sqrt(
//        (Math.pow(
//          (K + (AdvMotBridge.AdvGetActPos(0) / 1000.0 * 20.0)),
//          2.0
//        )) - (Math.pow(217.0 - t / 2, 2.0))
//      ) - Math.sqrt(Math.pow(K, 2.0) - (Math.pow(217 - e / 2, 2.0)))
//      var lb = 2 * kotlin.math.atan((2 * detalb) / e)
//      list1.add(1.0)
//      list2.add(2.0)
//      list3.add(3.0)
//    }

  }

}

