import com.sun.jna.ptr.ByReference;

public class IntArrayByReference extends ByReference {
  public int size;
  public static int unit = 4;
  public IntArrayByReference(int size) {

    super( size * unit);
    this.size = size;
  }

  public IntArrayByReference( ) {
    super(unit);
  }

  public int getValue(long offset){
    return this.getPointer().getInt(offset * unit);
  }
}
