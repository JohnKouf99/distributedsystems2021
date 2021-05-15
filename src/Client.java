import org.glassfish.jaxb.runtime.v2.runtime.output.SAXOutput;

import java.io.*;
import java.net.*;
import java.util.*;
import java.io.Serializable;


public class Client extends Thread implements Serializable {
    private static final long serialVersionUID = -1892561327013038124L;
    String tag;
    String tag2;
    String channelName;
    Socket requestSocket;
    ObjectInputStream in;
    ObjectOutputStream out;
    int port;
    VideoFile value;
    List <List<Object>>  info;
    Queue<byte[]> queue;// = new LinkedList<>();
    private ArrayList<Queue<byte[]> > QueueList = new ArrayList<>();

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
        //Socket requestSocket=null;
        Socket requestSocket2=null;
        InetAddress addr=null;
        int RealPort=0;
        List<Integer> list = Arrays.asList(BrokerPorts);
        int RandomPort =list.get(new Random().nextInt(list.size())); //we choose a random broker to initiate communication
        System.out.println(RandomPort);

        try{
            requestSocket = new Socket("127.0.0.1", RandomPort); //we connect to a random port

            //we get the BrokerInfo list
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());

            out.writeObject("info"); //inform the broker that we want info for the broker hashtags
            out.flush();

            this.info = new ArrayList<>();            //then we read the brokerinfo list
            this.info = (List <List<Object>>) in.readObject();
            System.out.println(info.size());

            if(in.readObject().toString().equals("List sent")){

                outerloop:
                for(List<Object> listt : this.info){

                    for(Object item: listt){

                        if(item instanceof InetAddress)
                            addr = (InetAddress)item;

                        if(item.equals(4222)||item.equals(4223)||item.equals(4224)){
                            RealPort = (Integer)item;
                        }

                        if(item.toString().contains(tag)){
                            System.out.println(tag+" is in port: "+RealPort+" and address: "+addr);
                        break outerloop;}

                    }
                }



                            if(RandomPort!=RealPort){
                                System.out.println("going to register now");
                                System.out.println(RealPort);
                                register(addr,RealPort,this.tag);


                                /**
                                System.out.println("sending my info....");
                                out.writeObject(new Client(this.tag,this.tag2,this.port,this.channelName)); //client registers to the broker that contains the info he needs //ΕΡΡΟΡ
                                out.flush();*/
                            }

                            else {
                                register(addr,RandomPort,this.tag);

                            } }

                    }

        catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } //catch (ClassNotFoundException e) {
        // e.printStackTrace();}

        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


            /**
            //info list iteration to find the port wanted
            for(List<Object> list2 : info){
                for(Object item: list2){

                    if(item instanceof InetAddress)
                        addr = (InetAddress)item;

                    if(item.equals(4222)||item.equals(4223)||item.equals(4224)){
                        RealPort = (Integer)item;  }


                    if(item.toString().contains(tag)){
                        System.out.println(tag+" is in port: "+RealPort+" and address: "+addr);}}}

                        if(RealPort==RandomPort){   //if the random broker we connected to is the broker the client wants

                            out.writeObject(new Client(this.tag,this.tag2,this.port,this.channelName)); //client registers to the broker that contains the info he needs
                            out.flush();
                                                   }

                        else{

                            requestSocket2 = new Socket("127.0.0.1", RealPort);
                            out = new ObjectOutputStream(requestSocket2.getOutputStream());
                            in = new ObjectInputStream(requestSocket2.getInputStream());
                            out.writeObject(new Client(this.tag,this.tag2,this.port,this.channelName)); //client registers to the broker that contains the info he needs //ΕΡΡΟΡ
                            out.flush();


                        }*/
            }

        /**
        finally {
            try {

                System.out.println("closing..... IN GETBROKERS");
                in.close();
                out.close();

                requestSocket.close();

                //System.out.println(Arrays.asList(info.get(0)));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }*/



//this method is used for client registry to a specific broker
      private void register(InetAddress addr,int port,String tag){
          synchronized (this){
        Socket request2 = null;

          System.out.println(Arrays.asList(info.get(0)));
        //ObjectOutputStream out=null;
        //ObjectInputStream in=null;
          System.out.println("im in register now");



              try{
                        System.out.println("connecting to the broker i want: "+port);
                        request2 = new Socket("127.0.0.1",port);
                        out = new ObjectOutputStream(request2.getOutputStream());
                        in = new ObjectInputStream(request2.getInputStream());
                        out.writeObject("register");
                        out.flush();
                        out.writeObject(new Client(this.tag,this.tag2,this.port,this.channelName)); //client registers to the broker that contains the info he needs //ΕΡΡΟΡ
                        System.out.println("sending my info....");
                        out.flush();
                        if(in.readObject().equals("got consumer info")){
                            System.out.println("info sent successfully");
                        }












                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                  e.printStackTrace();
              } finally {
                        try {
                            System.out.println("closing.... IN REGISTER");
                            in.close();
                            out.close();
                            request2.close();
                            requestSocket.close();
                            acceptvideo();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }


                }}













    private synchronized void acceptvideo(){
        Socket frombroker=null;
        ObjectOutputStream out=null;
        ObjectInputStream in = null;
        ServerSocket serverSocket=null;


        try{
            serverSocket = new ServerSocket(this.port);
            while(true){
                frombroker = serverSocket.accept();
                out = new ObjectOutputStream(frombroker.getOutputStream());
                in = new ObjectInputStream(frombroker.getInputStream());






                //while exw akoma video na dexthw
                while (true){
                    Object obj = in.readObject();
                if(!(obj instanceof Integer)){ break;}
                int size = (Integer) obj;
                if(size!=0){
                    System.out.println("the number of chunks sent is: "+size+"-1");

                    queue = new LinkedList<>(); //for each video we initialize a new queue
                    //store the video to queue
                    for(int i=0; i<size-1; i++){
                        queue.add((byte[]) in.readObject());
                    }
                    byte[] obj2 = (byte[])in.readObject();
                    String channel = new String(obj2);
                    System.out.println("From channel: " + channel +" and im: "+ this.getName());
                    QueueList.add(queue);

                }
            }
            }



    } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            try {
                System.out.println("closing acceptvideo...");
                out.close();
                in.close();
                frombroker.close();
                serverSocket.close();
                return;
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }


    }

    public int getPort() {
        return port;
    }

    public String getChannelName() {
        return channelName;
    }


    public synchronized void run() {
        //connect();

           getBrokers();
           //register(this.tag);
           //register(this.tag);
           //register(this.tag2);



   }

    public static void main(String[] args) {

        new Client("#sea",null,4111, "Kostakis").start();
        new Client("#nature",null,4112, "Kostas").start();

        //new Client("#nature","#sea",4112, "Kostaras").start();

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

