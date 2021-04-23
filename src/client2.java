import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

public class client2 extends Thread {
    int a;


    client2(int a) {
        this.a=a;
    }

    public void run() {

        Socket requestSocket = null;


        ObjectOutputStream out = null;

        ObjectInputStream in = null;
        try {


            requestSocket = new Socket("127.0.0.1", 4321);

            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());
            out.writeObject(a);
            out.flush();
            System.out.println("i send this to the server: " +this.a);
            out.writeObject(this.a);
            //Ανάγνωση ενός αντικειμένου Integer από τον διακομιστή μέσω του in ObjectInputStream
            //System.out.println("Client>" + in.readInt());

        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                in.close();	out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public static void main(String args[]) {
        //εκκινεί 10 νήματα όπου το κάθε ένα στέλνει 2 διαφορετικούς αριθμούς στον server για άθροιση

        new Client(10,4321).start();
        new Client(20,4321).start();
        new Client(30,4321).start();
        // new Client(40, 5).start();
        //new Client(50, 5).start();
        // new Client(60, 5).start();
        // new Client(70, 5).start();
        // new Client(80, 5).start();
        //new Client(90, 5).start();
        //new Client(100, 5).start();
    }
}










