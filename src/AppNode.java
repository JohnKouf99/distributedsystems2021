import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;





public class AppNode extends Thread {


    public AppNode(){
        new Client(5,4323).start();
        new Client(10,4323).start();
        new Server(4333).openServer();
        new Server(4334).openServer();
    }


    public static void main(String[] args) {
        new Client(5,4323).start();
        new Client(10,4323).start();
        new Server(4333).openServer();
        new Server(4334).openServer();

    }
}
