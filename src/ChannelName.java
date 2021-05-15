import org.apache.poi.ss.formula.functions.Na;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChannelName {
    String channelName;
    List<String> hashtagsPublished = new ArrayList<String>();
    HashMap<String, ArrayList<VideoFile>> usersVideoFilesMap;


//we create a random chanel named John Pap
    public ChannelName(String channelName){
        usersVideoFilesMap = new HashMap<>();
        if(channelName=="John"){
             this.channelName = channelName;
             this.usersVideoFilesMap = setUsersVideoFilesMap();

             this.hashtagsPublished.add("#nature");
             this.hashtagsPublished.add("#city");
             this.hashtagsPublished.add("#sea");


    }


        if(channelName=="Nikolas"){
            this.channelName = channelName;
            this.usersVideoFilesMap = setUsersVideoFilesMap();

            this.hashtagsPublished.add("#city");
            this.hashtagsPublished.add("#sea");

        }



        if(channelName=="Euthimis"){
            this.channelName = channelName;
            this.usersVideoFilesMap = setUsersVideoFilesMap();

            this.hashtagsPublished.add("#nature");
            this.hashtagsPublished.add("#sea");

        }











    }

// we initialize the channels videos and hashtags
    public HashMap setUsersVideoFilesMap() {
        //if the channel name is John, set these videos to be on his channel, with the appropriate hashtags
        if(this.channelName=="John"){
        ArrayList<VideoFile> NatureVideos = new ArrayList<>();
        ArrayList<VideoFile> CityVideos = new ArrayList<>();
        ArrayList<VideoFile> SeaVideos = new ArrayList<>();

        NatureVideos.add(new VideoFile("mp4files/EarthExample.mp4"));
        NatureVideos.add(new VideoFile("mp4files/BeachExample.mp4"));
        SeaVideos.add(new VideoFile("mp4files/BeachExample.mp4"));
        CityVideos.add(new VideoFile("mp4files/TrafficExample.mp4"));

        //we associate hashtags with the video list
        this.usersVideoFilesMap.put("#nature", NatureVideos) ;
        this.usersVideoFilesMap.put("#city", CityVideos);
        this.usersVideoFilesMap.put("#sea", SeaVideos);
        //we also associate channelName with video list
        this.usersVideoFilesMap.put(this.channelName, NatureVideos);
        this.usersVideoFilesMap.put(this.channelName, CityVideos);
        this.usersVideoFilesMap.put(this.channelName, SeaVideos);




        return this.usersVideoFilesMap;}

        //we do the same with two other channells too
        if(this.channelName=="Nikolas"){
            ArrayList<VideoFile> CityVideos = new ArrayList<>();
            ArrayList<VideoFile> SeaVideos = new ArrayList<>();


            SeaVideos.add(new VideoFile("mp4files/SeaVideo.mp4"));
            CityVideos.add(new VideoFile("mp4files/EmeraldCity.mp4"));
            CityVideos.add(new VideoFile("mp4files/TrafficExample.mp4"));

            //we associate hashtags with the video list
            this.usersVideoFilesMap.put("#city", CityVideos);
            this.usersVideoFilesMap.put("#sea", SeaVideos);
            //we also associate channelName with video list
            this.usersVideoFilesMap.put(this.channelName, CityVideos);
            this.usersVideoFilesMap.put(this.channelName, SeaVideos);

            return this.usersVideoFilesMap;


        }



        if(this.channelName=="Euthimis"){
            ArrayList<VideoFile> NatureVideos = new ArrayList<>();
            ArrayList<VideoFile> SeaVideos = new ArrayList<>();

            NatureVideos.add(new VideoFile("mp4files/ForestExample.mp4"));
            NatureVideos.add(new VideoFile("mp4files/WaterfallVideo.mp4"));
            SeaVideos.add(new VideoFile("mp4files/WaterfallVideo.mp4"));
            SeaVideos.add(new VideoFile("mp4files/BeachExample.mp4"));
            SeaVideos.add(new VideoFile("mp4files/SeaVideo.mp4"));
            this.usersVideoFilesMap.put("#nature", NatureVideos);
            this.usersVideoFilesMap.put(this.channelName, NatureVideos);
            this.usersVideoFilesMap.put(this.channelName, SeaVideos);

            return this.usersVideoFilesMap;

        }

        else return null;
    }




    public String getChannelName() {
        return channelName;
    }

    public HashMap getUsersVideoFilesMap() {
        return usersVideoFilesMap;
    }

    //this is for testing
    public static void main(String[] args) {

        ChannelName channel = new ChannelName("John");

        //channel.usersVideoFilesMap.add

    }


}
