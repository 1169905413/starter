import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;

public class ADBridge {

  public static native boolean ad_init();
  public static native boolean ad_motor_on();
  public static native boolean ad_motor_off();
  public static native boolean ad_motor_start();
  public static native boolean ad_motor_stop();
  public static native boolean ad_set_param(double freq, double amp);
  public static native boolean ad_set_param2(double freq, double amp, double amp2);

  public static native double apos();
  public static native double bpos();

  public static native int pwrTemp();
  public static native int readInport();

  public static native void setapos(double pos);
  public static native void setbpos(double pos);
  public static native boolean AdvdoSetBit(int i,int channel, boolean bit);
  public static native short AdvdoGetBit(int i, int channel);
  public static native short AdvdiGetBit(int i, int channel);
  public static native boolean AdvResetError(int i);
  public static native boolean AdvOpenAxis();
  public static native boolean Advsvon();
  public static native boolean Advsvoff();
  public static native boolean AdvGpResetError();


  static{
    Native.setProtected(true);
//    Native.register( "C:\\Users\\jiali\\source\\repos\\CANUtil\\x64\\Release\\Bridge2.dll");
    Native.register( "lib\\Bridge2.dll");

  }

  public static void main(String[] args) {
    ADBridge.ad_init();
  }


}
