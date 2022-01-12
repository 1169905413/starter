import com.sun.jna.Library;

public interface Clib extends Library {
  int Nb(int a, int b);
  void Aout(double dv, int chan);
  void Init();
}
