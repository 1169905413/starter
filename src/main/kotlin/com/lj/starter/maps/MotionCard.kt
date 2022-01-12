package com.lj.starter.maps

import IntArrayByReference
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.json.JsonObject
import java.lang.Math.*
import java.util.logging.Logger

class MotionCard (val station: STATION): AbstractVerticle(){
  val logger = Logger.getLogger(this.toString())
  var init:Boolean = false
  val count = 500
  val channel = 3
  var angred = 0.0
  var num1 = 0
  var pipe = 0.0
  var num = 0.0
  var buffers:ArrayList<IntArrayByReference> = ArrayList(3)
  var list1 = mutableListOf<Double>()
  var list2 = mutableListOf<Double>()
  var list3 = mutableListOf<Double>()
  var list4 = mutableListOf<MutableList<Double>>()
  override fun start(startPromise: Promise<Void>) {
    logger.info("Deploy MotionCard!")
    init = AdvMotBridge.AdvInit()
    ADBridge.ad_init()
    Agito.home()
    if(init) {
      for(i in 0 until channel){
        buffers.add(IntArrayByReference(count))
      }
//      AdvMotBridge.AdvStartDaq()

//      vertx.setPeriodic(5100){
//        for(i in 0 until channel){
//          val buffer = buffers[i]
//          val len = AdvMotBridge.AdvGetDaqData(i,buffer)
//          val temp_buffer = arrayListOf<Int>()
//            for (j in 0 until len) {
//              val value = buffer.getValue(j.toLong())
//              temp_buffer.add(value)
//          }
//          station.data[i] = temp_buffer
//        }
//        AdvMotBridge.AdvStartDaq()
//      }




      vertx.setPeriodic(5000){
        list4.clear()

        for(i in 1 until list1.size){
          list1[i] = (list1[i]*0.4)+(list1[i-1]*0.6)*1.02
          list2[i] = (list2[i]*0.4)+(list2[i-1]*0.6)*1.02
          list3[i] = (list3[i]*0.4)+(list3[i-1]*0.6)*1.02
        }
        for(i in 0 until list1.size){
          list1[i] = String.format("%.2f",list1[i]).toDouble()
          list2[i] = String.format("%.2f",list2[i]).toDouble()
          list3[i] = String.format("%.2f",list3[i]).toDouble()
        }
        list4.add(list1)
        list4.add(list2)
        list4.add(list3)
//        list4 = STATUS.station.low_pass_ao(list4)
        list1 = mutableListOf<Double>()
        list2 = mutableListOf<Double>()
        list3 = mutableListOf<Double>()
      }

      vertx.setPeriodic(10){
        var K = 185.25+(STATUS.station.com_K*(5/4))+ STATUS.station.offsetb
        var k1 = 285.0+(STATUS.station.com_k1*(5/4))+ STATUS.station.offsete
        var k2 = 285.0+(STATUS.station.com_k2*(5/4))+ STATUS.station.offsete
        var R = ((AdvMotBridge.AdvGetActPos(2)+ STATUS.station.com_R)/5000.0)*360.0
        var t = pipe+num
        var lbate = acos((76429.0 - pow(k2+(AdvMotBridge.AdvGetActPos(1)/50.0), 2.0))/(200.0*sqrt(66429.0)))- acos((76429.0 - pow(k1, 2.0))/(200.0*sqrt(66429.0)))
        var l3 = acos(t / 350.0)
        var l4 = l3 - lbate
        var e = 350 * cos(l4)
//        println("com_k1======${STATUS.station.com_k1}")
//        println("com_k2======${STATUS.station.com_k2}")
//        println("k1======${k1}")
//        println("k2======${k2}")
//        println("lbate=======$lbate")
//        println("acos((76429.0 - pow(k2+(AdvMotBridge.AdvGetActPos(1)/50.0), 2.0))/(200.0*sqrt(66429.0)))=======${acos((76429.0 - pow(k2+(AdvMotBridge.AdvGetActPos(1)/50.0), 2.0))/(200.0*sqrt(66429.0)))}")
//        println("acos((76429.0 - pow(k1, 2.0))/(200.0*sqrt(66429.0)))=======${acos((76429.0 - pow(k1, 2.0))/(200.0*sqrt(66429.0)))}")
//        println("l3=====$l3")
//        println("l4====$l4")
//        println("e=====$e")
        var ppu = e*(AdvMotBridge.AdvGetActPos(1)/1000.0*20.0)
        var percent_e = (e / t - 1)*100
        var detalb = sqrt((pow((K + (AdvMotBridge.AdvGetActPos(0)/1000.0*20.0)), 2.0)) - (pow(217.0 - e/ 2, 2.0))) - sqrt(pow(K, 2.0) - (pow( 217- e/2 , 2.0)))
        var lb = 2 * kotlin.math.atan((2 * detalb) / e)

        list1.add(String.format("%.2f",lb*180/ PI).toDouble())
        list2.add(String.format("%.2f",percent_e).toDouble())
        list3.add(String.format("%.2f",R).toDouble())
//        list1.add(lb*180/ PI)
//        list2.add(percent_e)
//        list3.add(R)
      }
      vertx.setPeriodic(100){
        for( i in 0 until channel){
          val last_pos = station.act_pos[i]
          val last_direction = station.act_direction[i]
          val pos = AdvMotBridge.AdvGetActPos(i)
          if(abs(pos - last_pos) > 1){
            val direction = pos > last_pos
            if(last_direction != direction){
              station.act_count[i] ++
            }
            station.act_direction[i] = direction
            station.act_pos[i] = pos
          }
        }
        station.meter = station.get_count()
      }
    }
  }



  fun start_move (body:JsonObject):JsonObject{
    var json  = JsonObject()
    station.apply {
      rot = body.getDouble("Rotation_aml")
      ext = body.getDouble("Extention_aml")
      bend = body.getDouble("Bend_aml")
      vmax1 = body.getDouble("vmax1")
      vmax2 = body.getDouble("vmax2")
      acc = body.getDouble("acc")
        if (STATUS.station.com_K + (bend / 1000 * 16) > 96) {
          json.put("nb1", "Bend amplifier exceeded limitation.")
          return json
        }
        if (STATUS.station.com_k2 + (ext / 1000 * 16) < (-55)) {
          println(com_k2)
          println((ext / 1000 * 16))
          json.put("nb1","Extention amplifier exceeded limitation.")
          return json
        }
        if (STATUS.station.com_R + (rot / 5000 * 16) > 15) {
          json.put("nb1","Rotation amplifier exceeded limitation.")
          return json
        }
      val axis1 = bend
      val axis2 = ext
      val axis3 = rot
//      AdvMotBridge.AdvSetHome(i)
      AdvMotBridge.AdvSetAcc(acc)

      if( ext >0.0 ||  bend >0.0 || rot >0.0) {
        AdvMotBridge.AdvClearPath()
        AdvMotBridge.AdvAddPath(true,false,0.0,0.0,0.0,0.0)
        var count = 0
        if(ext + bend > 0.0){
          AdvMotBridge.AdvAddPath(true,false,vmax1, axis1,-axis2,0.0)
          AdvMotBridge.AdvAddPath(true,false,vmax1,-axis1,axis2,0.0)
          count += 2
        }

        if(rot > 0.0){
          AdvMotBridge.AdvAddPath((ext + bend) <=0.0,false,vmax2,0.0,0.0,axis3)
          AdvMotBridge.AdvAddPath(true,false,vmax2,0.0,0.0,-axis3)
          count += 2
        }
        AdvMotBridge.AdvAddPath(true,true,vmax2,0.0,0.0,0.0)
        AdvMotBridge.AdvStart(count)
      }
    }
    return json
  }

  fun set_pos(i:Int, pos:Double){
    AdvMotBridge.AdvMoveAbstPos(i,pos)
  }

  fun set_home(i:Int){
    AdvMotBridge.AdvSetHome(i)
  }

  fun stop_move () {
    AdvMotBridge.AdvStop()
    Thread.sleep(1000)
    AdvMotBridge.AdvMoveAbstPos(0,0.0)
    AdvMotBridge.AdvMoveAbstPos(1,0.0)
    AdvMotBridge.AdvMoveAbstPos(2,0.0)
  }

}



