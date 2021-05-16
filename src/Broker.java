
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import java.io.*;
import java.net.InetAddress;
import java.net.*;
import java.util.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Broker extends Thread implements Serializable {

    //Connection stuff
    ServerSocket fromclient=null;
    Object obj;
    Socket connection;
    static Integer ServerPorts[] = new Integer[] { 4333, 4334, 4345};

    //broker stuff
    public static List<Broker> BrokerList = new ArrayList<Broker>();
    static List<String> BrokerHashes =new ArrayList<>();
    static HashMap<String, Broker> BrokerHashesMap = new HashMap<>();
    int port; //client-broker communication
    int srvrport; //random port to take info from (not values)
    String brokerhash;
    InetAddress addr;
    int brokerID;
    List<String> infotaken = new ArrayList<>();


    //client stuff
    String hashtag;
    List<String> hashtagList = new ArrayList<String>();
    List<String> channelList = new ArrayList<>();
    HashMap<Integer,Integer> info = new HashMap<>();
    Queue<Client> registeredClients = new LinkedList<>();


    //Server stuff
    public static ListMultimap<String , Integer> ServerPortmap =  ArrayListMultimap.create(); //tag->server port hashmap
     Queue<byte[]> queue; //video chunks are stored here
     ArrayList<Queue<byte[]> > QueueList; //videos of a specific key type are stored here
     Map<String, ArrayList<Queue<byte[]> >> multimap = new HashMap<>();  //tag -> video hashmap



    public Broker(int port, int srvrport){
        try {
            this.addr = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.brokerID=addBrokerValues(this.port, this.addr.getHostAddress()); //brokerID= IP + Port
        this.port=port;
        this.srvrport=srvrport; //random port to take info from (not values)
        this.brokerhash = encryptThisString(String.valueOf(addBrokerValues(this.port, "127.0.0.1"))); //IP+Port hashing
        BrokerHashes.add(this.brokerhash);
        BrokerHashesMap.put(this.brokerhash, this);
        BrokerList.add(this);

    }





    public synchronized void run(){

        notifyBrokersOnChanges();

        SendInfoToClient();



    }




    void pull(String tag, ObjectOutputStream out, ObjectInputStream in){

        try {


            ArrayList<Queue<byte[]> > list = multimap.get(tag); // get list of queues(=list of list videos ) with specific tag
            for(Queue<byte[]> video : list){  //iterate throught each video of this tag
                int size = video.size(); //get video size
                out.writeObject(size); //we send the size to the consumer
                out.flush();
                for(int i=0; i<size-1; i++){  //for each chunk of the video
                    out.writeObject(video.remove()); //send it to the client
                    out.flush();
                }

                out.writeObject(video.remove()); //send the channel name seperately
            }

            out.flush();
            out.writeObject("end");
            out.flush();




        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    private synchronized void CommunicateWithPublisher(){

        Socket request = null;
        ObjectOutputStream out=null;
        ObjectInputStream in=null;



        for(Client cl : registeredClients){
            String tag= cl.tag; //take the tag from the first client

            try{
                if(ServerPortmap.get(cl.tag)==null) {
                    System.out.println("Server doesnt have this tag");
                    return;
                }
                List<Integer> ports = ServerPortmap.get(cl.tag); //we take all the servers that contain this tag
                //check if info is already taken
                if(infotaken.contains(tag)) return;
                for(Integer port : ports){
                    System.out.println("Broker"+this.port+ ": Taking info from server port: "+port);
                    request = new Socket("127.0.0.1",port);
                    out = new ObjectOutputStream(request.getOutputStream());
                    in = new ObjectInputStream(request.getInputStream());
                    out.writeObject(tag);
                    out.flush();


                    synchronized (request){



                    while(true){

                        Object obj = in.readObject();

                        if(!(obj instanceof Integer)){
                            break;}

                        int size  = (Integer) obj; //we read the number of chunks

                        if(size!=0){
                            System.out.println("Broker"+this.port+ ": the number of chunks sent is: "+size);


                            queue = new LinkedList<>(); //for each video we initialize a new queue
                            //store the video to queue
                            for(int i=0; i<size; i++){
                                queue.add((byte[]) in.readObject());
                            }

                            byte[] obj2 = (byte[])in.readObject();
                            queue.add((byte[]) obj2); //lastly we recieve the channelname at the end of the queue


                            //add queue to queue list
                            if(multimap.containsKey(tag))
                                multimap.get(tag).add(queue);

                            else{

                                QueueList = new ArrayList<>(); //if there is no tag for the video we create a new queue list
                                QueueList.add(queue); // and store the video there
                                multimap.put(tag, QueueList);
                            }


                        }



                    }}



                     }

                     infotaken.add(tag);

                     in.close();
                     out.close();
                     request.close();
                     return;



            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

//method in which we initialize the port map(tag->server structure) and send back to the publisher the hashsed result
 void notifyBrokersOnChanges(){
        ServerSocket fromclient=null;
        Socket connection=null;
        ObjectOutputStream out=null;
        ObjectInputStream in=null;


        ListMultimap temp;
    try {
        fromclient = new ServerSocket(this.srvrport-10,10);
        connection = fromclient.accept();
        out = new ObjectOutputStream(connection.getOutputStream());
         in = new ObjectInputStream(connection.getInputStream());

        temp = (ListMultimap) in.readObject();

        for (Object key : temp.keySet()) {
            List<Integer> lastNames = temp.get(key);
            for(Integer port : lastNames){
            this.ServerPortmap.put((String)key, port);}
        }

        CalculateKeys(); //after we get the key info from the server we proceed to hash its content and each brokers gets its hashes
        List <List<Object>> BrokerInfo = new ArrayList<>();


            if(getBrokerList().size()==3){ //when the last broker has been initialized
                for(Broker br: getBrokerList() ){  //we send the broker list to the publisher
                    List <Object> info = new ArrayList<>();
                    info.add(br.addr);
                    info.add(br.port);
                    info.add(br.brokerID);
                    info.add(br.getChannelList());
                    info.add(br.getHashtagList());
                    BrokerInfo.add(info);

                }}
                out.writeObject(BrokerInfo);
                out.flush();

        in.close();
        out.close();



    } catch (IOException e) {
        try {
            fromclient.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    } catch (ClassNotFoundException e) {
        e.printStackTrace();
    }




}

         void SendInfoToClient(){

             ObjectOutputStream objectOutputStream=null;
             ObjectInputStream objectInputStream=null;
             Object obj2 = null;
             String tag=null;

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



                 }



                 if(obj.toString().equals("register")){

                     obj2 = objectInputStream.readObject();



                 if(obj2 instanceof Client){   //if a client wants to register
                     tag = ((Client) obj2).tag; // we hold the consumers key

                    if (!registeredClients.contains(obj2)) {
                        registeredClients.add((Client) obj2);
                    }


                    objectOutputStream.writeObject("got consumer info");



                }


                 }

                objectOutputStream.close();
                objectInputStream.close();
                CommunicateWithPublisher();
                SendVideoToConsumer();






                }


            }

            catch (IOException e) {
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
        }



        private void SendVideoToConsumer(){
            Socket request = null;
            ObjectOutputStream out=null;
            ObjectInputStream in=null;
            for(Client cl : registeredClients){
                if(this.hashtagList.contains(cl.tag)||this.channelList.contains(cl.tag)){
                    try{
                        request = new Socket("127.0.0.1",cl.port);
                        out = new ObjectOutputStream(request.getOutputStream());
                        in = new ObjectInputStream(request.getInputStream());
                        pull(cl.tag, out, in);


                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally {
                        try {
                            request.close();
                            in.close();
                            out.close();
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
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
            //for each hashed tag
            String hashedkey = encryptThisString(keys);
            if(hashedkey.compareTo(BrokerHashes.get(0))>0 ){ //if hash is bigger than biggest hash
                Broker br = BrokerHashesMap.get(BrokerHashes.get(2)); //we find the broker with the smallest hash
                if(keys.contains("#") && !br.getHashtagList().contains(keys)) br.getHashtagList().add(keys);
                else if(!br.getHashtagList().contains(keys)) br.getChannelList().add(keys);  //and add the hashtag to it
            }

            else if(hashedkey.compareTo(BrokerHashes.get(0))<0 && hashedkey.compareTo(BrokerHashes.get(1))>0){ //add to the bigger broker
                Broker br = BrokerHashesMap.get(BrokerHashes.get(0));
                if(keys.contains("#")&&!br.getHashtagList().contains(keys)) br.getHashtagList().add(keys);
                else if(!br.getHashtagList().contains(keys)) br.getChannelList().add(keys);
            }

            else if(hashedkey.compareTo(BrokerHashes.get(1))<0 && hashedkey.compareTo(BrokerHashes.get(2))>0){ //add to the second bigger broker
                Broker br = BrokerHashesMap.get(BrokerHashes.get(1));
                if(keys.contains("#")&&!br.getHashtagList().contains(keys)) br.getHashtagList().add(keys);
                else if(!br.getHashtagList().contains(keys)) br.getChannelList().add(keys);
            }

            else {
                Broker br = BrokerHashesMap.get(BrokerHashes.get(2)); //add to the last broker
                if(keys.contains("#")&&!br.getHashtagList().contains(keys)) br.getHashtagList().add(keys);
                else if(!br.getHashtagList().contains(keys)) br.getChannelList().add(keys);
            }




        }



        }




        else return;
    }




    public static void main(String[] args) {
        Thread br = new Broker(4222,4333);
        Thread br2 = new Broker(4223,4334);
        Thread br3 = new Broker(4224,4335);



        System.out.println("broker 1 starting...");
        br.start();
        System.out.println("broker 2 starting...");
        br2.start();
        System.out.println("broker 3 starting...");
        br3.start();



















    }}


