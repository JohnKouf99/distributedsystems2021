import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import javax.swing.plaf.ViewportUI;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Server extends Thread{

    ServerSocket srvrSocket;
    Socket request;
    Socket connection;

    int port;
    Broker br;
    VideoFile value;
    List<String> hashtags = new ArrayList<>();
    public static ListMultimap<String, Integer> Portmap = ArrayListMultimap.create(); // contains (hashtag , server port) pairs
    public static HashMap <String , Integer> BrokerPortMap = new HashMap<>(); // contains (hashtag , broker port) pairs
    public static List<List<Object>> BrokerList; //contains list of brokers
    static int servers=0;
    ChannelName channel;
    HashMap<String, ArrayList<VideoFile>> VideoMap; //contains (key, video) map

    boolean CLOSED = false;



    public Server(int port, String name, String video){
        this.port=port;
        if(video!=null)
            this.value = new VideoFile(video);

        if(name=="John"){
            this.channel = new ChannelName("John");
            this.VideoMap = this.channel.setUsersVideoFilesMap(); //initialize video map
            for ( String key : VideoMap.keySet() ) {     //initialize portmap
                Portmap.put(key,this.port);

            }
        }
        if(name=="Nikolas"){
            this.channel = new ChannelName("Nikolas");
            this.VideoMap = this.channel.setUsersVideoFilesMap();
            for ( String key : VideoMap.keySet() ) {     //initialize portmap
                Portmap.put(key,this.port);
            }
        }

        if(name=="Euthimis"){
            this.channel = new ChannelName("Euthimis");
            this.VideoMap = this.channel.setUsersVideoFilesMap();
        for ( String key : VideoMap.keySet() ) {     //initialize portmap
            Portmap.put(key,this.port);
            System.out.println(key+"  " +value);
        }
    }

        servers++;
        }



        public Server(int port){
            this.port = port;
        }


    public static HashMap<String, Integer> getBrokerPortMap() {
        return BrokerPortMap;
    }

    //adds new video to channel
    public HashMap addVideo(String tag, VideoFile value){
        if(VideoMap.containsKey(tag)){
            VideoMap.get(tag).add(value);
            return VideoMap;
        }

        else{
            ArrayList <VideoFile> list = new ArrayList();
            list.add(value);
            VideoMap.put(tag, list);
            return VideoMap;
        }
    }
        void removeHashtag(String hashtag){
            this.VideoMap.remove(hashtag);
        }

        void removeVideo(String tag, VideoFile value){
        ArrayList list = VideoMap.get(tag);

        if(list.contains(value))
            list.remove(value);

        if(list.isEmpty())
            VideoMap.remove(tag);
        //VideoMap.containsValue(list.contains(value));
        }



        List getServerList(){return this.hashtags;}

        List getBrokerList(){return br.getBrokerList();}


//we send the portmap to the brokers
        synchronized void notifyBrokers(String tag){

            Socket requestSocket=null;
            ObjectOutputStream out = null;
            ObjectInputStream in = null;

            try{
                     requestSocket = new Socket("127.0.0.1",this.port-10);

                     out = new ObjectOutputStream(requestSocket.getOutputStream());
                     in = new ObjectInputStream(requestSocket.getInputStream());


                     //if tag=null we just send the portmap with no changes
                    if(tag==null && servers==3){

                        out.writeObject(this.Portmap);
                        out.flush();

                        Object obj =(List) in.readObject(); //we read the broker list sent by broker
                        BrokerList = (List<List<Object>>) obj;



                    }
                    //if there is video for this certain key and all servers are open
                    else if(VideoMap.containsKey(tag) && tag!=null && servers==3){

                    Portmap.put(tag, this.port);
                    out.writeObject(this.Portmap);
                    out.flush();

                    Object obj =(List) in.readObject();
                    BrokerList =    (List <List<Object>>) in.readObject();

                    }

                    //if it is removed
                    else if( servers==3) {

                        Portmap.removeAll(tag);
                        out.writeObject(this.Portmap);
                        out.flush();

                        Object obj =(List) in.readObject();
                        BrokerList =    (List <List<Object>>) in.readObject();

                    }
                    else return;
            }
            catch (UnknownHostException unknownHost) {
                System.err.println("You are trying to connect to an unknown host server!");
            } catch (IOException ioException) {
                ioException.printStackTrace();} catch (ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    System.out.println("closing...");
                    in.close();
                    out.close();
                    requestSocket.close();
                    ConnectWithBroker();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

        }

       synchronized void ConnectWithBroker(){
        Socket frombroker=null;
        ObjectOutputStream out=null;
        ObjectInputStream in = null;
        ServerSocket serverSocket=null;
        try{
            System.out.println("im in connect with brokers now");

            serverSocket = new ServerSocket(this.port,10);

            int counter=0;

            while(true){
                frombroker = serverSocket.accept();
                out = new ObjectOutputStream(frombroker.getOutputStream());
                in = new ObjectInputStream(frombroker.getInputStream());
                String tag = (String)in.readObject();
                System.out.println("client wants "+tag+"'s videos"+" and my port is "+ this.port);
                push(tag,out,in);
                System.out.println("im ending push now...");



            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            try {
                System.out.println("closing ConnectWithBroker...");
                out.close();
                in.close();
                serverSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        }




    public static ListMultimap<String, Integer> getPortmap() {
        return Portmap;
    }





    public void run(){
       // notifyBrokers();



        notifyBrokers(null);





    }




    // this method sends to the broker a video with a specific tag or channelname
     synchronized void push(String tag,ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) throws IOException { //pass socket and stream as argument?

        //Socket requestSocket=null;
        //ObjectOutputStream objectOutputStream = null;
        //ObjectInputStream objectInputStream = null;

        try{

            for(String key : VideoMap.keySet()){ //for a certain hashtag
                if(key.equals(tag)){ //we find videos with specific tag
                    for(VideoFile video: VideoMap.get(key)) { //for each video that belongs to a specific tag
                        video.SplitToChunks();
                        System.out.println(video.getChunksList().size());
                        objectOutputStream.writeObject((Integer)video.getChunksList().size()); //we send the list size
                        objectOutputStream.flush();

                        for(byte[] arr : video.getChunksList()){
                            objectOutputStream.writeObject(arr);
                            objectOutputStream.flush();
                        }

                        byte[] b = this.channel.getChannelName().toString().getBytes();

                        objectOutputStream.writeObject(b);

                    }
                }
            }

            objectOutputStream.flush();
            objectOutputStream.writeObject("end");
            objectOutputStream.flush();





            if(!VideoMap.containsKey(tag)){
                System.out.println("Video does not exist");
                return;
            }







        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {


        new Server(4333,"John", null).start();
        new Server(4334,"Nikolas", null).start();
        new Server(4335,"Euthimis", null).start();

        /**
        for (String name: Portmap.keySet()) {
            String key = name.toString();
            String value = Portmap.get(name).toString();
            System.out.println(key + " " + value);
        }*/







    }







}