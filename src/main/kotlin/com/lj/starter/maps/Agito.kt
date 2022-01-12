package com.lj.starter.maps

object Agito {

  fun getPos(r:Boolean) = if(r)ADBridge.apos()else ADBridge.bpos()
  fun setPos(pos:Double, r:Boolean) {
    if(r)ADBridge.setapos(pos)else ADBridge.setbpos(pos)
  }

  fun movePos(pos:Double, r:Boolean) {
    setPos(getPos(r) + pos , r)
  }

  fun start_motor(){
//    home()
    ADBridge.ad_motor_start()
  }

  fun home(){
    ADBridge.ad_motor_on()
    ADBridge.setapos(0.0)
    ADBridge.setbpos(0.0)
  }

  fun motor_on(){
    ADBridge.ad_motor_on()
  }

  fun motor_off(){
    ADBridge.ad_motor_off()
  }


  fun stop_motor(){
    ADBridge.ad_motor_stop()
    home()
  }

  fun set_param(freq:Double,amp:Double){
    ADBridge.ad_set_param(freq, amp *1000)
  }




}
