package com.lj.starter

import com.lj.starter.Modbus.READ_AI
import com.lj.starter.Modbus.READ_DO
import io.vertx.core.buffer.Buffer
import java.net.CacheResponse


object Modbus {

  const val READ_DO = 1.toByte()
  const val READ_DI = 2.toByte()
  const val READ_AO = 3.toByte()
  const val READ_AI = 4.toByte()
  const val WRITE_S_DO = 5.toByte()
  const val WRITE_S_AO = 6.toByte()
  const val WRITE_M_DO = 15.toByte()
  const val WRITE_M_AO = 16.toByte()
  private const val SUB_ADDR = 1.toByte()

  fun request(index : Int, action : Byte, addr:Int, count:Int ): Buffer = Buffer
    .buffer(byteArrayOf(
      hbyte(index), lbyte(index),
      0,0,
      0,6,
      SUB_ADDR,action,
      hbyte(addr), lbyte(addr),
      hbyte(count), lbyte(count)
      ))
  fun len(resp: ByteArray) = b2i(resp[4],resp[5])
  fun index(resp: ByteArray) = b2i(resp[0],resp[1])
  fun action(resp: ByteArray) = resp[7]
  fun value(resp: ByteArray, count:Int) = b2i(resp[count * 2 + 9],resp[count * 2 + 10])
  fun valueudp(resp: ByteArray, cycle: Int,channel:Int) = b2i(resp[channel * 120 + 15  + cycle * 2],resp[channel * 120 + 16 + cycle * 2])

  fun b2i(a:Byte, b:Byte) = ( a.toInt() shl 8) or (b.toInt()  and 0xff)
  fun b2i(a:Int,b:Int, c:Int, d:Int) = (a and 0xff shl 24 ) or (b and 0xff shl 16) or (c and 0xff shl 8) or ( d and 0xff)
  fun b2i(a:Byte,b:Byte, c:Byte, d:Byte) = (a.toInt() and 0xff shl 24 ) or (b.toInt() and 0xff shl 16) or (c.toInt() and 0xff shl 8) or ( d.toInt() and 0xff)

  fun b2f(a:Int,b:Int, c:Int, d:Int) = Float.fromBits(b2i(a,b,c,d))
  fun i2b(i:Int) = mutableListOf((i shr 24 and 0xff), (i shr 16 and 0xff),(i shr 8 and 0xff),(i and 0xff))
  fun i2b16(i:Int) = mutableListOf((i shr 8 and 0xff),(i and 0xff))

  fun f2b(f:Float) = i2b(f.toBits())

  fun hbyte(b:Int) = (b and 0xff00 shr 8).toByte()
  fun lbyte(b:Int) = (b and 0x00ff).toByte()
  val msg : (Int,Int,Int,Int,Int,Int,Int,Int)->ByteArray =
    { a,b,c,d
      ,e,f,g,h ->
      byteArrayOf(a.toByte(),
        b.toByte(),
        c.toByte(),
        d.toByte(),
        e.toByte(),
        f.toByte(),
        g.toByte(),
        h.toByte()
      ) }
}

