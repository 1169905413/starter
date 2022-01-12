import com.sun.jna.Native;

public class AdvMotBridge {
  public static native boolean AdvInit();

  public static native boolean AdvSetAcc(double acc);
  public static native boolean AdvStop();
  public static native boolean AdvStart(int num);
  public static native boolean AdvAddPath(boolean blending, boolean end, double FH, double ep0, double ep1, double ep2);
  public static native boolean AdvClearPath();
  public static native double AdvGetActPos(int i);
  public static native boolean AdvSetHome(int i);
  public static native boolean AdvMoveAbstPos(int i, double d);
  public static native boolean AdvStartDaq();
  public static native boolean AdvStopDaq();
  public static native int AdvGetDaqData(int i, IntArrayByReference buffer);
//  public static native int AbortJob(int nClientId);

  static{
    Native.setProtected(true);
//    Native.register( "C:\\Users\\jiali\\source\\repos\\CANUtil\\x64\\Release\\Bridge2.dll");
    Native.register( "lib\\Bridge2.dll");

  }
  public static void main(String[] args) {
  }
}
