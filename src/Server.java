import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Server extends Thread{

    ServerSocket srvrSocket;
    int port;
    Socket connection;
    Broker br;
    List<String> hashtags = new ArrayList<>();
    private static HashMap <String , Integer> map = new HashMap<>(); // contains (hashtag , serverport) pairs


    public Server(int port, String tag1,String tag2, String tag3){
        this.port=port;
        addHashtag(tag1);
        addHashtag(tag2);
        addHashtag(tag3);
        map.put(tag1,this.port);
        map.put(tag2,this.port);
        map.put(tag3,this.port);

        }

        public Server(){}



        void addHashtag(String hashtag){
            this.hashtags.add(hashtag);
        }

        void removeHashtag(String hashtag){
            this.hashtags.remove(hashtag);
        }

        List getServerList(){return this.hashtags;}

        List getBrokerList(){return br.getBrokerList();}

        void notifyBrokers(String str){
        if(!map.containsKey(str))
            map.put(str,this.port);
        else map.remove(str);
        }

    public  HashMap<String, Integer> getMap() {
        return map;
    }

    public void run(){
        openServer();
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

    public static void main(String[] args) {
        //testing HashMap
        new Server(4333,"a","b","c");
        new Server(4334,"aa","bb","cc");
        new Server(4335,"aaa","bbb","ccc");
        Server s = new Server();
        System.out.println(Arrays.asList(s.getMap()));



    }







}