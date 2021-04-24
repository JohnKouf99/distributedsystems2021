import org.bouncycastle.util.IPAddress;
import java.net.InetAddress;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
public class Broker extends Thread{
    //Connection stuff
    ServerSocket fromclient=null;
    Object obj;
    Socket connection;
    ObjectInputStream ClientIn;
    ObjectOutputStream ClientOut;

    //broker stuff
    List<Broker> BrokerList = new ArrayList<Broker>();
    int port;
    int srvrport;
    String brokerhash;

    String hashtag;

    public Broker(int port, int srvrport){
        this.port=port;
        this.srvrport=srvrport;
        this.brokerhash = setBrokerHash(this,"127.0.0.1");
        BrokerList.add(this);

    }



    public void run(){
        acceptfromclient();



    }
/**
    public Broker(Socket connection, int port){

        this.port=port;


        try {
             ClientOut = new ObjectOutputStream(connection.getOutputStream());

             ClientIn = new ObjectInputStream(connection.getInputStream());


        } catch (IOException e) {
            e.printStackTrace();}
    }*/



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
                 this.obj = sendtoserver();
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

        }




        Object sendtoserver(){
            try {
                System.out.println("Broker: Trying to connect to server...");
                Socket request = new Socket("127.0.0.1",this.srvrport);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(request.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(request.getInputStream());
                System.out.println("Broker: Im sending this to the server: "+ this.obj+ " ' ");
                objectOutputStream.writeObject(this.obj+"'");
                objectOutputStream.flush();
                this.obj = objectInputStream.readObject();
                System.out.println("Broker: the servers response was: "+ this.obj);






            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return this.obj;

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

    void setHashtag(String hashtag){this.hashtag=hashtag;}

    String getBrokerhash(){return this.brokerhash;}

    String setBrokerHash(Object br, String addr){
        int a = iptoint(addr);
        int b = ((Broker) br).port;
        int c = a+b;

        return encryptThisString(String.valueOf(c));

    }







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
        Thread br = new Broker(4321,4333);
        Thread br2 = new Broker(4323,4333);
        Thread br3 = new Broker(4325,4334);
       // Thread br2 = new Broker();

        br.start();
        br2.start();
        br3.start();



        System.out.println(((Broker) br).getBrokerhash());







    }}


