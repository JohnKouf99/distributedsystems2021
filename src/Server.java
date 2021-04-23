import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

public class Server{
    ServerSocket srvrSocket;
    int port;
    Socket connection;
    Broker br;

    public Server(int port){this.port=port;}


    void openServer(){
        ObjectInputStream serverinputstream=null;
        ObjectOutputStream serveroutstream = null;

        try{
            srvrSocket = new ServerSocket(this.port,10);

            while(true){

                connection = srvrSocket.accept();
                System.out.println("Server: server accepted connection");


                Thread t = new ActionsForClients(connection);

                t.start();


            }



        }
        catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host server!");
        } catch (IOException ioException) {
            ioException.printStackTrace();}
            finally {
            try {

                srvrSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }


    }


    //public static void main(String[] args) {
       // new Server().openServer();
    //}

}