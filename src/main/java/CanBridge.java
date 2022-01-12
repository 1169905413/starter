import com.sun.jna.Library;
import com.sun.jna.ptr.IntByReference;

import java.util.ArrayList;


public interface CanBridge extends Library {
  int Test(int a, int b);
  boolean Init(int timeout);
  int Read(int len, IntByReference num,IntArrayByReference lengths,ByteByReference prt);
  boolean Send(int cob, int id, int len, byte data[]);
}
