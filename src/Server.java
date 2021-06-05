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
    String name;
    ChannelName channel;
    HashMap<String, ArrayList<VideoFile>> VideoMap; //contains (key, video) map
    HashMap<String, ArrayList<VideoFile>> newVideoMap = new HashMap<>(); //contains (key, video) map
    static List<Server> ServerList = new ArrayList<>();

    public Server(){}

    public Server(int port, String name){
        this.port=port;
        this.name = name;


        if(name=="John"){
            ServerList.add(this);
            this.channel = new ChannelName("John");
            this.VideoMap = this.channel.setUsersVideoFilesMap(); //initialize video map
            for ( String key : VideoMap.keySet() ) {     //initialize portmap
                Portmap.put(key,this.port);

            }
        }
        if(name=="Nikolas"){
            ServerList.add(this);
            this.channel = new ChannelName("Nikolas");
            this.VideoMap = this.channel.setUsersVideoFilesMap();
            for ( String key : VideoMap.keySet() ) {     //initialize portmap
                Portmap.put(key,this.port);
            }
        }

        if(name=="Euthimis"){
            ServerList.add(this);
            this.channel = new ChannelName("Euthimis");
            this.VideoMap = this.channel.setUsersVideoFilesMap();
            for ( String key : VideoMap.keySet() ) {     //initialize portmap
                Portmap.put(key,this.port);
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
    void addVideo(String name,String tag, VideoFile value){

        for(Server s: ServerList){

        if(name.equals(s.name)){
            System.out.println("want to add video and im "+s.name);
            if(s.newVideoMap.containsKey(tag)){
                System.out.println("adding video....");
                s.newVideoMap.get(tag).add(value);
                s.VideoMap.get(tag).add(value);
                Portmap.put(tag, s.port);}

            else{
                System.out.println("adding video2....");
                ArrayList <VideoFile> list = new ArrayList();
                list.add(value);
                s.newVideoMap.put(tag, list);
                s.VideoMap.put(tag, list);
                Portmap.put(tag, s.port);
            }




        }



        else return;


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
            System.out.println("opening publisher for brokers...");

            serverSocket = new ServerSocket(this.port,10);

            int counter=0;

            while(true){
                frombroker = serverSocket.accept();
                out = new ObjectOutputStream(frombroker.getOutputStream());
                in = new ObjectInputStream(frombroker.getInputStream());
                String tag = (String)in.readObject();

                if(tag.equals("extra")){
                    //System.out.println("EXTRAEXTRAEXTRAEXTRAEXTRAEXTRAEXTRAEXTRA");

                    String tag2 = (String)in.readObject();

                    if(this.newVideoMap.isEmpty()){

                        out.writeObject("null");
                        out.flush();


                }

                    else {

                        System.out.println("EXTRA VIDEO");
                        //push(this.VideoMap,tag2,out,in);
                        push(this.newVideoMap,tag2,out,in);
                        newVideoMap.clear();
                                   }




                }
                else{
                System.out.println(this.channel.channelName+": client wants "+tag+"'s videos");



                push(this.VideoMap,tag,out,in);

                System.out.println(this.channel.channelName+": ending push process...");
                if(this.name=="John") {
                    System.out.println("AND IM IN CONNECT WITH BROKER "+ this.newVideoMap.size());
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
                System.out.println(this.channel.channelName+ ": closing broker connection...");
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

    // this method sends to the broker a video with a specific tag or channelname
    synchronized void push(HashMap<String, ArrayList<VideoFile>> VideoMap,String tag,ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) throws IOException { //pass socket and stream as argument?

        //Socket requestSocket=null;
        //ObjectOutputStream objectOutputStream = null;
        //ObjectInputStream objectInputStream = null;

        try{

            for(String key : VideoMap.keySet()){ //for a certain hashtag
                if(key.equals(tag)){ //we find videos with specific tag
                    for(VideoFile video: VideoMap.get(key)) { //for each video that belongs to a specific tag
                        video.SplitToChunks();
                        objectOutputStream.writeObject((Integer)video.getChunksList().size()); //we send the list size
                        objectOutputStream.flush();

                        for(byte[] arr : video.getChunksList()){
                            objectOutputStream.writeObject(arr);
                            objectOutputStream.flush();
                        }

                        String extradata = this.channel.getChannelName() +","+ video.getVideoName();

                        byte[] b = this.channel.getChannelName().toString().getBytes();
                        byte[] b1 = extradata.toString().getBytes();

                        objectOutputStream.writeObject(b1);
                        objectOutputStream.flush();






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





    public void run(){


        notifyBrokers(null);


    }





    public static void main(String[] args) {


        new Server(4333,"John").start();
        new Server(4334,"Nikolas").start();
        new Server(4335,"Euthimis").start();



    }







}