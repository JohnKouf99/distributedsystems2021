import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Arrays;


public class AppNode extends Thread {







    public static void main(String[] args) {



        new Server(4333,"John", null).start();
        new Server(4334,"Nikolas", null).start();
        new Server(4335,"Euthimis", null).start();


        new Client("#sea",null,4111, "Kostakis").start();
        new Client("#nature",null,4112, "Kostas").start();








    }
}
