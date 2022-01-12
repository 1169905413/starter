package com.lj.starter

import com.lj.starter.maps.MAPS
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise

class test4 : AbstractVerticle(){
  override fun start(startPromise: Promise<Void>) {
    calculate(10.0)
  }
  fun calculate(num:Double){
    var x = 100
    var l = Math.sqrt((165.0*165.0)+(198.0*198.0))
    var t = 200
    var e1 = 0.0
    var e2 = 0.0
    var deltae = e2-e1
    var k = 0.0
    var q = 39.80557
    var detalb=0.0
    var bending=2* Math.tan((t - deltae) / (2 * detalb))
  }

}
