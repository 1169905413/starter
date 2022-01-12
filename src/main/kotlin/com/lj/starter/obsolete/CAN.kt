package com.lj.starter.obsolete

import ByteByReference
import CanBridge
import IntArrayByReference
import com.sun.jna.Native
import com.sun.jna.ptr.IntByReference
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import java.util.logging.Logger

object CAN: AbstractVerticle() {
  private val logger: Logger = Logger.getLogger(this.toString())
  val step = 1L
  val timeout = 1500

  override fun start(startPromise: Promise<Void>?) {
    logger.info("Deploy CAN")
    val lib: CanBridge = Native.loadLibrary(
      "lib\\Bridge2.dll",
//      "C:\\Users\\jiali\\source\\repos\\CANUtil\\x64\\Release\\Bridge2.dll",
      CanBridge::class.java
    )

    if(lib.Init(timeout)){
      val len = 256;
      val buffer = ByteByReference(len * 8)
      val num = IntByReference(0)
      val lengths = IntArrayByReference(len)

      vertx.setPeriodic(step) {
        val ret = lib.Read(len,num,lengths,buffer)
        if(num.value == 0){
          logger.info("No message. Code :$ret")
        } else{
          for(i in 0 until num.value){
            val l = lengths.getValue(i.toLong())
            for (j in 0 until l){
              print(buffer.getValue((i * 8 + j).toLong()).toString() + " ")
            }
            println()
          }
        }
      }



      vertx.setPeriodic(5000) {
        lib.Send(0,0, 8,  byteArrayOf(0,4,0,0,0,6, 0x71, 0xD9.toByte()))
      }

    } else{
      logger.severe("Failed to deploy CAN")
    }




  }

}
