import java.io.Serializable;

public class Message implements Serializable {

    private static final long serialVersionUID = -2723363051271966964L;
    int a;
    int b;

    public Message(int a, int b) {
        super();
        this.a = a;
        this.b = b;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public String toString() {
        return "a: " + a + ", b: " + b;
    }
}
