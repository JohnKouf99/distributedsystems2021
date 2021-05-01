import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChannelName {
    String channelName;
    List<String> hashtagsPublished = new ArrayList<String>();
    HashMap usersVideoFilesMap ;


//we create a random chanel named John Pap
    public ChannelName(String channelName){

        if(channelName=="John pap"){
        this.channelName = channelName;
        this.usersVideoFilesMap = setUsersVideoFilesMap();

        this.hashtagsPublished.add("#nature");
        this.hashtagsPublished.add("#earth");


    }}

// we initialize the channels videos and hashtags
    public HashMap setUsersVideoFilesMap() {
        //if the channel name is John, set these videos to be on his channel, with the appropriate hashtags
        if(this.channelName=="John"){
        ArrayList<VideoFile> NatureVideos = new ArrayList<>();
        ArrayList<VideoFile> CityVideos = new ArrayList<>();
        ArrayList<VideoFile> SeaVideos = new ArrayList<>();

        NatureVideos.add(new VideoFile("mp4/EarthExample"));
        NatureVideos.add(new VideoFile("mp4/BeachExample"));
        SeaVideos.add(new VideoFile("mp4/BeachExample"));
        CityVideos.add(new VideoFile("mp4/TrafficExample"));

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
        if(this.channelName=="Nikolas"){return null;} //TO DO
        if(this.channelName=="Euthimis"){return null;} //TO DO

        else return null;
    }



//this is for testing
    public static void main(String[] args) {

        ChannelName channel = new ChannelName("John");

        //channel.usersVideoFilesMap.add

    }


}
