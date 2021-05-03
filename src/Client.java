import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;



public class Client extends Thread{
    String a;
    //Socket requestSocket;
    ObjectInputStream in;
    ObjectOutputStream out;
    int port;
    VideoFile value;

    public Client(String a, int port){
        this.a=a;
        this.port=port;

    }



    void connect(){
        Socket requestSocket=null;
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;
        try{
            requestSocket = new Socket("127.0.0.1", this.port);


            objectOutputStream = new ObjectOutputStream(requestSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(requestSocket.getInputStream());


            System.out.println("Client: I send this to the server: " +this.a);
            objectOutputStream.writeObject(this.a);
            objectOutputStream.flush();
            System.out.println("Client: I got this from the server via the broker: "+objectInputStream.readObject());




        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
         finally {
            try {
                objectInputStream.close();
                objectOutputStream.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }


    void acceptpull(){

       Socket connection=null;
        int counter=0;
        try{
            ServerSocket srvrSocket = new ServerSocket(this.port,10);
            connection = srvrSocket.accept();

            ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(connection.getInputStream());

            while (in.readObject()!=null){

                System.out.println(counter++);

            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }








   public void run() {
        //connect();

           acceptpull();



   }

    public static void main(String[] args) {
        new Client("#pets",4323).start();
    }





}













































/**

public class Client extends Thread {
    Message msg; // = "Client says hello";

    public Client(int msg){
        this.msg = new Message(msg);
    }

    public void test(){

        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try{

            requestSocket = new Socket("127.0.0.1", 4321);
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());
            out.writeObject(this.msg);
            out.flush();
            System.out.println("o server leei: " +  in.readInt());




        }
        catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }  finally {
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
        System.out.println("starting...");
       Client cl = new Client(10);
       cl.test();
        /*new Client("test1").start();
        new Client("test2").start();
        new Client("test3").start();
        new Client("test4").start();
        new Client("test5").start();
        new Client("test6").start();
        new Client("test7").start();
        new Client("test8").start();

    }
}*/

