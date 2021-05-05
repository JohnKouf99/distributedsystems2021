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
    public static HashMap <String , Integer> Portmap = new HashMap<>(); // contains (hashtag , serverport) pairs
    ChannelName channel;
    HashMap<String, ArrayList<VideoFile>> VideoMap; //contains (key, value) map



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
        }
    }
        }



        public Server(int port){
            this.port = port;
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
        void notifyBrokers(String tag){

            Socket requestSocket=null;
            ObjectOutputStream out = null;
            ObjectInputStream in = null;



            try{
                     requestSocket = new Socket("127.0.0.1",this.port);
                     out = new ObjectOutputStream(requestSocket.getOutputStream());
                     in = new ObjectInputStream(requestSocket.getInputStream());


                     //if tag=null we just send the portmap with no changes
                    if(tag==null){
                        out.writeObject(this.Portmap);
                        out.flush();

                    }


                    //if there is video for this certain key
                    if(VideoMap.containsKey(tag) && tag!=null){

                    Portmap.put(tag, this.port);
                    out.writeObject(this.Portmap);
                    out.flush();}


                    //if it is removed
                    else {
                        Portmap.remove(tag);
                        out.writeObject(this.Portmap);
                        out.flush();


                    }






            }
            catch (UnknownHostException unknownHost) {
                System.err.println("You are trying to connect to an unknown host server!");
            } catch (IOException ioException) {
                ioException.printStackTrace();}


            finally {
                try {

                    in.close();
                    out.close();
                    requestSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }



        }




        /**
        void MapAdd(String str){
            if(!map.containsKey(str))
                map.put(str,this.port);

        }

        void MapRemove(String str){
             map.remove(str);
        }*/

/**
        void notifyBrokers(){
             //str=null;//to do

            try {
                Socket infoSocket = new Socket("127.0.0.1",4100);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(infoSocket.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(infoSocket.getInputStream());
                objectOutputStream.writeObject(getMap());
                objectOutputStream.flush();
                objectOutputStream.close();
                objectInputStream.close();
                infoSocket.close();


            } catch (IOException e) {
                e.printStackTrace();
            }


        }*/

    public static HashMap<String, Integer> getPortMap() {
        return Portmap;
    }

    public void run(){
       // notifyBrokers();
        //openServer();
        notifyBrokers(null);

        /**try {
            push("#nature", this.value);

        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }



    void openServer(){
        ObjectInputStream serverinputstream=null;
        ObjectOutputStream serveroutstream = null;

        try{
            srvrSocket = new ServerSocket(this.port,10);

            while(true){

                connection = srvrSocket.accept();
                System.out.println("Server: server accepted connection");
                serveroutstream = new ObjectOutputStream(connection.getOutputStream());
                serverinputstream = new ObjectInputStream(connection.getInputStream());
               // System.out.println("Server: sending map....");
               // serveroutstream.writeObject(getMap());
                //System.out.println(Arrays.asList(getMap()));
                String msg = (String)serverinputstream.readObject();
                System.out.println("Server: what i got from broker is: "+msg);
                msg="here is ur "+ msg +"video";
                serveroutstream.writeObject(msg);


                System.out.println("what im sending to the broker is: "+ msg);
                serveroutstream.flush();


                serverinputstream.close();
                serveroutstream.close();
                connection.close();




           }
        }
        catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host server!");
        } catch (IOException ioException) {
            ioException.printStackTrace();} catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {

                srvrSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }


    }

    void push(String tag1, VideoFile value) throws IOException {

        Socket requestSocket=null;
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;

        try{
            requestSocket = new Socket("127.0.0.1", this.port);


            objectOutputStream = new ObjectOutputStream(requestSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(requestSocket.getInputStream());
            objectOutputStream.writeObject(tag1);
            //objectOutputStream.flush();
            //objectOutputStream.writeObject(tag2);
            //objectOutputStream.writeObject(tag3);

            if(value!=null){
                //System.out.println("test");
                value.SplitToChunks();
                objectOutputStream.writeInt(value.getChunksList().size()); //we send the list size
                objectOutputStream.flush();


                for(byte[] arr : value.getChunksList()){
                    objectOutputStream.writeObject(arr);
                    objectOutputStream.flush();
                }}

            else objectOutputStream.writeObject(null);


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
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

    public static void main(String[] args) {

       // new Client("#pets",4323).start();
       // new Client("#food",4324).start();



       // System.out.println("server1");
        new Server(4333,"John", null).start();

        //System.out.println("server2");
        //new Server(4334,"aa","bb","cc").start();
        //System.out.println("server3");
       // new Server(4335,"aaa","bbb","ccc").start();


      //ChannelName channel = new ChannelName("John Pap"); // we create a new channel




    }







}