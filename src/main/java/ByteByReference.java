import com.sun.jna.Native;
import com.sun.jna.ptr.ByReference;

public class ByteByReference extends ByReference {
  public int size;
  public ByteByReference( int size) {

    super( size);
    this.size = size;
  }

  public ByteByReference( ) {
    super(1);
  }
  public byte getValue(long offset) {
    return this.getPointer().getByte(offset);
  }


}
