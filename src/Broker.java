
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.net.InetAddress;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Broker extends Thread{
    //Connection stuff
    ServerSocket fromclient=null;
    Object obj;
    Socket connection;
    ServerSocket serverinfo;
    ObjectInputStream ClientIn;
    ObjectOutputStream ClientOut;

    //broker stuff
    static List<Broker> BrokerList = new ArrayList<Broker>();
    int port; //client-broker communication
    int srvrport; //broker - server communication
    String brokerhash;
    InetAddress addr;
    int brokerID;


    Queue<VideoFile> values = new LinkedList<>();



    //client stuff
    String hashtag;
    List<String> hashtagList = new ArrayList<String>();
    List<ChannelName> channels = new ArrayList<>();




    //Server stuff
    //static Server s = new Server();
    public static HashMap<String , Integer> ServerPortmap = new HashMap<>(); //tag->server port hashmap
     Queue<byte[]> queue = new LinkedList<>();
     ArrayList<Queue<byte[]> > QueueList = new ArrayList<>();
     Map<String, ArrayList<Queue<byte[]> >> multimap = new HashMap<>();  //tag -> video hashmap



    public Broker(int port, int srvrport){
        try {
            this.addr = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.brokerID=addBrokerValues(this.port, this.addr.getHostAddress()); //brokerID= IP + Port
        this.port=port;
        this.srvrport=srvrport;
        this.brokerhash = encryptThisString(String.valueOf(addBrokerValues(this.port, "127.0.0.1"))); //IP+Port hashing
        BrokerList.add(this);

    }



    public void run(){
      //  getInfo();

       notifyBrokersOnChanges();


    }


    void pull(String tag){

        try {


            Socket request = new Socket("127.0.0.1",this.port);

            ObjectOutputStream out = new ObjectOutputStream(request.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(request.getInputStream());

            ArrayList<Queue<byte[]> > list = multimap.get(tag); // get list of queues(=list of list video chunks) with specific tag
            for(Queue<byte[]> video : list){  //iterate throught each video of this tag
                int size = video.size();
                for(int i=0; i<size; i++){  //for each chunk of the video
                    out.writeObject(video.remove());
                    out.flush();
                }  //send it to the client
            }

            out.writeObject(null);




        } catch (IOException e) {
            e.printStackTrace();
        }


    }

//method in which we initialize the port map
    void notifyBrokersOnChanges(){
        HashMap temp;
        try {
            fromclient = new ServerSocket(this.srvrport,10);
            connection = fromclient.accept();
            ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
            temp = (HashMap) in.readObject();

            for (Object key : temp.keySet()) {
                this.ServerPortmap.put((String)key, (Integer)temp.get(key));

            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            try {
                for (String name: ServerPortmap.keySet()) {
                    String key = name.toString();
                    String value = ServerPortmap.get(name).toString();
                    System.out.println(key + " " + value);
                }
                fromclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    //this method sends the info that the client has requested
    void sendInfo(){

    }





        /**
        void acceptfromclient(){
        try{

            fromclient = new ServerSocket(this.port,10);

            while (true){

                 this.connection = fromclient.accept();

                ObjectOutputStream objectOutputStream = new ObjectOutputStream(connection.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(connection.getInputStream());
                 this.obj =  objectInputStream.readObject();
                 this.hashtag = (String) this.obj;
                 System.out.println("Broker: What i got from client is: "+this.obj);
                 //this.obj = sendtoserver();
                 objectOutputStream.writeObject(this.obj);
                 System.out.println("Broker: I send this to the client: "+this.obj);

                 objectInputStream.close();
                 objectOutputStream.close();
                 connection.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            try {

                fromclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        }*/




       /** Object sendtoserver(){
            try {
                System.out.println("Broker: Trying to connect to server...");
                Socket request = new Socket("127.0.0.1",this.srvrport);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(request.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(request.getInputStream());
               // System.out.println("getting map....");
                //this.brokermap = (HashMap<String, Integer>) objectInputStream.readObject();
                System.out.println("Broker: Im sending this to the server: "+ this.obj+ " ' ");
                objectOutputStream.writeObject(this.obj+"'");
                objectOutputStream.flush();
                this.obj = objectInputStream.readObject();
                //System.out.println(Arrays.asList(brokermap));
                System.out.println("Broker: the servers response was: "+ this.obj);


            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return this.obj;

        }*/



    void acceptpush(){
        try{



            fromclient = new ServerSocket(this.srvrport,10);
            //String tag;

            while (true){

                this.connection = fromclient.accept();

                ObjectOutputStream objectOutputStream = new ObjectOutputStream(connection.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(connection.getInputStream());
                //hashtagList.add((String)objectInputStream.readObject());
                //hashtagList.add((String)objectInputStream.readObject());
                //hashtagList.add((String)objectInputStream.readObject());

                String tag = (String)objectInputStream.readObject(); //we read the tag string
                int size  = (Integer) objectInputStream.readInt();


                if(size!=0){


                System.out.println(size);


                for(int i=0; i<size; i++){
                    queue.add((byte[]) objectInputStream.readObject());
                }

                if(multimap.get(tag)!=null)
                    multimap.get(tag).add(queue);

                else{
                    QueueList.add(queue);
                    multimap.put(tag, QueueList);
                }

                    // this is for testing
                   // System.out.println(multimap.get(tag).get(0).size());
                    //System.out.println(multimap.get(tag));

                }


                objectInputStream.close();
                objectOutputStream.close();
                connection.close();
                break;




            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            try {

                fromclient.close();
            } catch (IOException e){
                e.printStackTrace();
            }


        }

    }







    public static String encryptThisString(String input)
    {
        try {
            // getInstance() method is called with algorithm SHA-1
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            // return the HashText
            return hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    String getPort(){
            return String.valueOf(this.port);
    }

    String getObj(){
            return String.valueOf(this.obj);
    }

    String getHashtag(){
            return this.hashtag;
    }

    public Map<String, ArrayList<Queue<byte[]>>> getMultimap() {
        System.out.println(multimap.get("#nature"));
        return multimap;

    }

    void setHashtag(String hashtag){this.hashtag=hashtag;}

    String getBrokerhash(){return this.brokerhash;}

    public List<Broker> getBrokerList(){return this.BrokerList;}


    // IP+Port calculation
    Integer addBrokerValues(int port, String addr){
        int a = iptoint(addr);
        int b = this.port;
        int c = a+b;

        return c;

    }






    //IP address to integer
    static Integer iptoint(String address){
        int result = 0;
        try {
            InetAddress addr = InetAddress.getByName(address);
            for (byte b: addr.getAddress())
            {
                result = result << 8 | (b & 0xFF);
            }

            return result;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return null;


    }




















    public static void main(String[] args) {
        Thread br = new Broker(4323,4333);
       // Thread br2 = new Broker(4324,4334);
       // Thread br3 = new Broker(4325,4335);
       // Thread br2 = new Broker();


        System.out.println("broker 1 starting...");
        br.start();


       // System.out.println("broker 2 starting...");
       // br2.start();
       // System.out.println("broker 3 starting...");
        //br3.start();
        //System.out.println(Arrays.asList(Server.getMap()));












    }}


