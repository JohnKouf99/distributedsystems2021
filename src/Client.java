import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.io.Serializable;


public class Client extends Thread implements Serializable {
    private static final long serialVersionUID = -1892561327013038124L;
    String tag;
    String tag2;
    String channelName;
    //Socket requestSocket;
    ObjectInputStream in;
    ObjectOutputStream out;
    int port;
    VideoFile value;
    List <List<Object>>  info = new ArrayList<>();

    Integer BrokerPorts[] = new Integer[] { 4222, 4223, 4224};

    public Client(String tag, String tag2, int port, String channelName){
        this.tag=tag;
        this.tag2=tag2;
        this.port=port;
        this.channelName=channelName;

    }

    public List <List<Object>> getInfo() {
        return info;
    }



    void disconnect(){}



    void getBrokers(){ // string tag to be added
        Socket requestSocket=null;
        //ObjectOutputStream out = null;
        //ObjectInputStream in = null;
        List<Integer> list = Arrays.asList(BrokerPorts);
        int port =list.get(new Random().nextInt(list.size())); //we choose a random broker to initiate communication
        System.out.println(port);



        try{
            requestSocket = new Socket("127.0.0.1", port);


            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());
            this.info = (List <List<Object>>) in.readObject();
            System.out.println(info.size());






        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } //catch (ClassNotFoundException e) {
           // e.printStackTrace();}

        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        /**
        finally {
            try {
                System.out.println("closing.....");
                in.close();
                out.close();
                requestSocket.close();

                //System.out.println(Arrays.asList(info.get(0)));


            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }*/
    }

//this method is used for client registry to a specific broker
    void register(String tag){
        Socket request = null;
        int port=0;
        InetAddress addr=null;
        ObjectOutputStream out=null;
        ObjectInputStream in=null;

        for(List<Object> list : info){
            for(Object item: list){

                if(item instanceof InetAddress)
                    addr = (InetAddress)item;

                if(item.equals(4222)||item.equals(4223)||item.equals(4224)){
                    port = (Integer)item;  }


                if(item.toString().contains(tag)){
                    System.out.println(tag+" is in port: "+port+" and address: "+addr);}}


                    try{
                        request = new Socket(addr,port);
                        out = new ObjectOutputStream(request.getOutputStream());
                        in = new ObjectInputStream(request.getInputStream());
                        Client client = this;
                        out.writeObject(client); //client registers to the broker that contains the info he needs //ΕΡΡΟΡ
                        out.flush();




                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    /**
                    finally {
                        try {
                            in.close();
                            out.close();
                            request.close();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }*/


                }

            }











    void acceptpull(){

       Socket connection=null;
        int counter=0;
        try{
            ServerSocket srvrSocket = new ServerSocket(4223,10);
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

    public int getPort() {
        return port;
    }

    public String getChannelName() {
        return channelName;
    }


    public void run() {
        //connect();

           getBrokers();
           register(this.tag);
           //register(this.tag2);



   }

    public static void main(String[] args) {
        new Client("#nature","#sea",4111, "Kostakis").start();
        //new Client("#nature","#sea",4112, "Kostas").start();

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

