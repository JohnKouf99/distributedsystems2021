

public class AppNode extends Thread {

    public static void wait(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }









    public static void main(String[] args) {

        Server s = new Server();

        new Server(4333,"John").start();
        new Server(4334,"Nikolas").start();
        new Server(4335,"Euthimis").start();


        wait(1000);
        new Client("#sea",4111, "client1",false).start();

        wait(2000);

        s.addVideo("John","#sea",new VideoFile("mp4files/food.mp4"));


        wait(1000);

        new Client("#sea",4112, "client2",false).start();


    }
}