import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Arrays;


public class AppNode extends Thread {

static Server s = new Server();


    public AppNode(){
        new Client("#food",4323).start();
        new Client("#pets",4324).start();
       // new Server(4333).openServer();
       // new Server(4334).openServer();
    }


    public static void main(String[] args) {

         new Client("#pets",4323).start();
        new Client("#food",4324).start();

        System.out.println("server1");
        new Server(4333,"a","b","c").start();
        System.out.println("server2");
        new Server(4334,"aa","bb","cc").start();
        System.out.println("server3");
        new Server(4335,"aaa","bbb","ccc").start();

        //Server s = new Server();
        System.out.println(Arrays.asList(s.getMap()));




    }
}
