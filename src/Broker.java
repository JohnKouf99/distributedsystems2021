
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.javatuples.Quartet;

import java.io.*;
import java.net.InetAddress;
import java.net.*;
import java.util.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Broker extends Thread {

    //Connection stuff
    ServerSocket fromclient=null;
    Object obj;
    Socket connection;
    ServerSocket serverinfo;
    ObjectInputStream ClientIn;
    ObjectOutputStream ClientOut;

    //broker stuff
    public static List<Broker> BrokerList = new ArrayList<Broker>();
    static List<String> BrokerHashes =new ArrayList<>();
    static HashMap<String, Broker> BrokerHashesMap = new HashMap<>();
    int port; //client-broker communication
    int srvrport; //broker - server communication
    String brokerhash;
    InetAddress addr;
    int brokerID;


    Queue<VideoFile> values = new LinkedList<>();




    //client stuff
    String hashtag;
    List<String> hashtagList = new ArrayList<String>();
    List<String> channelList = new ArrayList<>();
    HashMap<Integer,Integer> info = new HashMap<>();
    List<Client> registeredClients = new ArrayList<>();




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
        BrokerHashes.add(this.brokerhash);
        BrokerHashesMap.put(this.brokerhash, this);
        BrokerList.add(this);

    }

    public Broker(){}



    public void run(){

        notifyBrokersOnChanges();

        CalculateKeys();

        SendInfoToClient();

        //acceptConnection();







    }

    //this method gets info from server
    void getInfo(){
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

                    //store the video to queue
                    for(int i=0; i<size; i++){
                        queue.add((byte[]) objectInputStream.readObject());
                    }
                    //add queue to queue list
                    if(multimap.get(tag)!=null)
                        multimap.get(tag).add(queue);

                    else{
                        QueueList.add(queue);
                        multimap.put(tag, QueueList);
                    }


                    if(tag.contains("#") && !hashtagList.contains(tag)){
                        hashtagList.add(tag);
                }

                    if(!tag.contains("#")&& !channelList.contains(tag))
                        channelList.add(tag);


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

//method in which we initialize the port map(tag->server structure)
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
        in.close();
        out.close();



    } catch (IOException e) {
        try {
            fromclient.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return;
        //e.printStackTrace();
        //System.out.println("this is the eofffffffffffff");
    } catch (ClassNotFoundException e) {
        e.printStackTrace();
    }

    finally {
        try {
            /**for (String name: ServerPortmap.keySet()) {
             String key = name.toString();
             String value = ServerPortmap.get(name).toString();
             System.out.println(key + " " + value);
             }*/

            fromclient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

         void SendInfoToClient(){

             ObjectOutputStream objectOutputStream=null;
             ObjectInputStream objectInputStream=null;

        try{

            fromclient = new ServerSocket(this.port,10);



            while (true){

                 this.connection = fromclient.accept();
                List <List<Object>> BrokerInfo = new ArrayList<>();
                objectOutputStream = new ObjectOutputStream(connection.getOutputStream());
                objectInputStream = new ObjectInputStream(connection.getInputStream());
                Object obj=objectInputStream.readObject();


                 if(obj.toString().equals("info")){  //if we want the broker to send just info about its tags


                if(getBrokerList().size()==3){
                    System.out.println(getBrokerList().size());
                for(Broker br: getBrokerList() ){
                    List <Object> info = new ArrayList<>();
                    info.add(br.addr);
                    info.add(br.port);
                    info.add(br.brokerID);
                    info.add(br.getChannelList());
                    info.add(br.getHashtagList());
                    BrokerInfo.add(info);

                }}

                objectOutputStream.writeObject(BrokerInfo);
                objectOutputStream.flush();
                objectOutputStream.writeObject("List sent");
                objectOutputStream.flush();
                System.out.println("SIZEEEEEEEEEE: "+BrokerInfo.size());









                 }



                 if(obj.toString().equals("register")){

                    Object obj2 = objectInputStream.readObject();

                if(obj2 instanceof Client){   //if a client wants to register

                    if (!registeredClients.contains(obj2)) {
                        registeredClients.add((Client) obj2);
                    }

                    for(Client cl: registeredClients){
                        System.out.println("this is: "+cl.channelName);
                    }
                    System.out.println("=======================================");



                }}


                System.out.println("closing...");
                objectOutputStream.close();
                objectInputStream.close();



                //break;



                }


                //System.out.println("closing streams1...");
                 //objectInputStream.close();
               // System.out.println("closing streams2...");
                 //objectOutputStream.close();
                //System.out.println("closing streams3...");
                 //connection.close();

            }

            catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            try {
                System.out.println("final closing////");
                fromclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        }





      //this method accepts client requests and insterts the client to clientList
        synchronized void acceptConnection(){
        ServerSocket fromclient=null;
        ObjectOutputStream out=null;
        ObjectInputStream in=null;
        Socket connection;
        try{

            fromclient = new ServerSocket(this.port,10);

            while(true) {
                connection = fromclient.accept();
                out = new ObjectOutputStream(connection.getOutputStream());
                in = new ObjectInputStream(connection.getInputStream());


                Client client = (Client) in.readObject();
                if (!registeredClients.contains(client)) {
                    registeredClients.add(client);
                }

                for(Client cl: registeredClients){
                    System.out.println("this is: "+cl.channelName);
                }
                System.out.println("=======================================");

                out.close();
                in.close();
                connection.close();
                fromclient.close();


            }





        } catch (IOException e) {

            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
       // finally {
         //   try {
                //System.out.println("closing.....");
                //fromclient.close();
            //}  catch (IOException ioException) {
                //ioException.printStackTrace();
            //}
        //}



        }








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

        }

        //this method sends the info that the client has requested
        void sendInfo(){


        try {
        ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
        for(Object br : BrokerList){
        // info
        }

        } catch (IOException e) {
        e.printStackTrace();
        }

        }







        */



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

                //store the video to queue
                for(int i=0; i<size; i++){
                    queue.add((byte[]) objectInputStream.readObject());
                }
                //add queue to queue list
                if(multimap.get(tag)!=null)
                    multimap.get(tag).add(queue);

                else{
                    QueueList.add(queue);
                    multimap.put(tag, QueueList);
                }


                if(tag.contains("#") && !hashtagList.contains(tag)){
                    hashtagList.add(tag);
                }

                if(!tag.contains("#")&& !channelList.contains(tag))
                    channelList.add(tag);


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

    public List<String> getChannelList() {
        return channelList;
    }

    public List<String> getHashtagList() {
        return hashtagList;
    }

    public Map<String, ArrayList<Queue<byte[]>>> getMultimap() {
        System.out.println(multimap.get("#nature"));
        return multimap;

    }

    void setHashtag(String hashtag){this.hashtag=hashtag;}

    String getBrokerhash(){return this.brokerhash;}

    public static List<String> getBrokerHashes() {
        return BrokerHashes;
    }

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

//will be run if brokerlist.size==3

    void CalculateKeys(){

        if(getBrokerList().size()==3){


        //we start from the biggest hash
        BrokerHashes.sort(Comparator.naturalOrder());
        Collections.reverse(BrokerHashes);

        for(String keys: ServerPortmap.keySet()){
            //System.out.println("server port map size in calculate keys is: "+ServerPortmap.size());
            //for each hashed tag
            String hashedkey = encryptThisString(keys);
            if(hashedkey.compareTo(BrokerHashes.get(0))>0){ //if hash is bigger than biggest hash
                Broker br = BrokerHashesMap.get(BrokerHashes.get(2)); //we find the broker with the smallest hash
                if(keys.contains("#")) br.getHashtagList().add(keys);
                else br.getChannelList().add(keys);  //and add the hashtag to it
            }

            else if(hashedkey.compareTo(BrokerHashes.get(0))<0 && hashedkey.compareTo(BrokerHashes.get(1))>0){ //add to the bigger broker
                Broker br = BrokerHashesMap.get(BrokerHashes.get(0));
                if(keys.contains("#")) br.getHashtagList().add(keys);
                else br.getChannelList().add(keys);
            }

            else if(hashedkey.compareTo(BrokerHashes.get(1))<0 && hashedkey.compareTo(BrokerHashes.get(2))>0){ //add to the second bigger broker
                Broker br = BrokerHashesMap.get(BrokerHashes.get(1));
                if(keys.contains("#")) br.getHashtagList().add(keys);
                else br.getChannelList().add(keys);
            }

            else {
                Broker br = BrokerHashesMap.get(BrokerHashes.get(2)); //add to the last broker
                if(keys.contains("#")) br.getHashtagList().add(keys);
                else br.getChannelList().add(keys);
            }




        }}

        else return;
    }
















    public static void main(String[] args) {
        Thread br = new Broker(4222,4333);
        Thread br2 = new Broker(4223,4334);
        Thread br3 = new Broker(4224,4335);
       // Thread br2 = new Broker();


        System.out.println("broker 1 starting...");
        br.start();
        br2.start();
        br3.start();






       // System.out.println("broker 2 starting...");
       // br2.start();
       // System.out.println("broker 3 starting...");
        //br3.start();
        //System.out.println(Arrays.asList(Server.getMap()));












    }}


