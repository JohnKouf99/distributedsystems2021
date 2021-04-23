import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

public class ActionsForClients extends Thread {
    ObjectInputStream in;
    ObjectOutputStream out;

    public ActionsForClients(Socket connection) {

        try {
            out = new ObjectOutputStream(connection.getOutputStream());

            in = new ObjectInputStream(connection.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {

        try {

            int msg = (Integer)in.readObject();
            System.out.println("Server: what i got from broker is: "+msg);
            msg+=10;
            out.writeObject(msg);

            System.out.println("what im sending to the broker is: "+ msg);
            out.flush();




        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
