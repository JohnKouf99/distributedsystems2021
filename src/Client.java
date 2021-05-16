import org.glassfish.jaxb.runtime.v2.runtime.output.SAXOutput;

import java.io.*;
import java.net.*;
import java.util.*;
import java.io.Serializable;


public class Client extends Thread implements Serializable {
    private static final long serialVersionUID = -1892561327013038124L;
    String tag;
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

    public Client(String tag,  int port, String channelName){
        this.tag=tag;
        this.port=port;
        this.channelName=channelName;

    }

    public List <List<Object>> getInfo() {
        return info;
    }




        void getBrokers(){ // string tag to be added
        InetAddress addr=null;
        int RealPort=0;
        List<Integer> list = Arrays.asList(BrokerPorts);
        int RandomPort =list.get(new Random().nextInt(list.size())); //we choose a random broker to initiate communication

        try{
            requestSocket = new Socket("127.0.0.1", RandomPort); //we connect to a random port

            //we get the BrokerInfo list
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());

            out.writeObject("info"); //inform the broker that we want info for the broker hashtags
            out.flush();

            this.info = new ArrayList<>();            //then we read the brokerinfo list
            this.info = (List <List<Object>>) in.readObject();
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
                            System.out.println(this.channelName+" :"+tag+" is in port: "+RealPort+" and address: "+addr);
                        break outerloop;}

                    }
                }



                            if(RandomPort!=RealPort){
                                System.out.println(this.channelName+":beginning register process...");
                                register(RealPort);


                            }

                            else {
                                register(RandomPort);

                            } }

                    }

        catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            try {

                System.out.println(this.channelName+"closing.....");
                in.close();
                out.close();

                requestSocket.close();


            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

            }






//this method is used for client registry to a specific broker
      private void register(int port){
          synchronized (this){

              Socket request2 = null;






              try{
                        System.out.println(this.channelName+": register to broker with port: "+port);
                        request2 = new Socket("127.0.0.1",port);
                        out = new ObjectOutputStream(request2.getOutputStream());
                        in = new ObjectInputStream(request2.getInputStream());
                        out.writeObject("register");
                        out.flush();
                        out.writeObject(new Client(this.tag,this.port,this.channelName)); //client registers to the broker that contains the info he needs //ΕΡΡΟΡ
                        System.out.println(this.channelName+": sending my info....");
                        out.flush();
                        if(in.readObject().equals("got consumer info")){
                            System.out.println(this.channelName+": info sent successfully");
                        }





                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                  e.printStackTrace();
              } finally {
                        try {
                            System.out.println(this.channelName+": ending registration process");
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






                //while there are still chunks of data to receive
                while (true){
                    Object obj = in.readObject();
                if(!(obj instanceof Integer)){ break;}
                int size = (Integer) obj;
                if(size!=0){
                    System.out.println(this.channelName+": the number of chunks received is: "+size);

                    queue = new LinkedList<>(); //for each video we initialize a new queue
                    //store the video to queue
                    for(int i=0; i<size-1; i++){
                        queue.add((byte[]) in.readObject());
                    }
                    byte[] obj2 = (byte[])in.readObject();
                    String channel = new String(obj2);
                    System.out.println(this.channelName+": From channel: " + channel);
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

           getBrokers();


   }

    public static void main(String[] args) {

        new Client("#sea",4111, "Client1").start();
        new Client("#nature",4112, "Client2").start();



    }





}


