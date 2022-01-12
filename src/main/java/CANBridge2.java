import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class CANBridge2 {

  public static native boolean Init(int timeout , int start, int num, int[] br);
  public static native int Read(int len, IntByReference num, IntArrayByReference lengths,IntArrayByReference cobs,IntArrayByReference ids, ByteByReference prt , int chan);
  public static native boolean Send(int cob, int id, int len, byte data[] , int chan);


  static{
    Native.setProtected(true);
//    Native.register( "C:\\Users\\jiali\\source\\repos\\CANUtil\\x64\\Release\\Bridge2.dll");
    Native.register( "lib\\Bridge2.dll");

  }

//  public static void main(String[] args) {
//     CANBridge2.Init(100,2,2);
//
//  }


}
