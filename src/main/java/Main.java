import com.sun.jna.Native;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import jssc.*;

public class Main {

  public static void main(String[] args) throws SerialPortException {

//    Clib lib = Native.loadLibrary("dll2",Clib.class);

//    System.out.println(lib.Nb(5, 7));
//    System.out.println(lib.Nb(5, 7));
//    System.out.println(lib.Nb(5, 7));
//    System.out.println(lib.Nb(5, 7));
    for (String portName : SerialPortList.getPortNames()) {
      System.out.println(portName);
    }

    SerialPort sp = new SerialPort("COM4");
    sp.openPort();

    sp.setParams(SerialPort.BAUDRATE_19200,  SerialPort.DATABITS_8,
    SerialPort.STOPBITS_1,
    SerialPort.PARITY_NONE);

    Vertx.vertx().setPeriodic(1000, new Handler<Long>() {
      @Override
      public void handle(Long aLong) {
//        System.out.println("NBBB");
        byte[] buffer = new byte[27];
        buffer[0]=(byte)0xAA;
        buffer[1]=(byte)0xFF;
        buffer[2]=(byte)0x3;
        buffer[4]=(byte)0x6;

        for(int i = 6 ; i < 16; i++){
          buffer[i]=(byte)i;
//          buffer[13]=(byte)0x1;
        }
        int[] a = new int[]{1,2,3};

        buffer[26]=(byte)0x55;
//        buffer[27] = '\r';
//        buffer[28] = '\n';
        try {
          sp.writeBytes(buffer);
        } catch (SerialPortException e) {
          e.printStackTrace();
        }
      }
    });
    sp.addEventListener(new SerialPortEventListener() {
      @Override
      public void serialEvent(SerialPortEvent serialPortEvent) {
        try {
          System.out.println("-------");
          for (byte b : sp.readBytes()) {
            System.out.print(b);
            System.out.print(" ");
          }
//          System.out.println();
        } catch (SerialPortException e) {
          e.printStackTrace();
        }
      }
    });

//    SerialPort sp4 = new SerialPort("COM4");
//
//    sp4.openPort();
//
//    sp4.setParams(SerialPort.BAUDRATE_19200,  SerialPort.DATABITS_8,
//      SerialPort.STOPBITS_1,
//      SerialPort.PARITY_NONE);
//
//    sp4.writeString("NB");
  }
}
