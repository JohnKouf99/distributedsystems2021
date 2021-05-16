import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Arrays;


public class AppNode extends Thread {

    public static void wait(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }







    public static void main(String[] args) {



        new Server(4333,"John").start();
        new Server(4334,"Nikolas").start();
        new Server(4335,"Euthimis").start();

        wait(1000);

        new Client("#sea",4111, "client1").start();
        new Client("#nature",4112, "client2").start();








    }
}
