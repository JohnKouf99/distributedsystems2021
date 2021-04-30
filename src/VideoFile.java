import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.tika.Tika;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;



public class VideoFile {

    private String videoName;
    private String channelName;
    private String dateCreated;
    private String length;
    private String framerate;
    private String frameWidth;
    private String frameHeight;
    private String file;
    ArrayList<String> associatedHashtags = new ArrayList<>();
    private static final int  KILOBYTE = 1024*100;
    byte[] videoFileChunk = new byte[KILOBYTE];
    ArrayList<byte[]> ChunksList = new ArrayList<byte[]>();


    public VideoFile(String file) throws TikaException, SAXException, IOException {
        this.file = file;
        this.getData();
    }







//extract video info such as name,date etc
    void getData() throws IOException,SAXException, TikaException {

        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        FileInputStream inputstream = new FileInputStream(new File(this.file));
        ParseContext pcontext = new ParseContext();

        MP4Parser MP4Parser = new MP4Parser();
        MP4Parser.parse(inputstream, handler, metadata,pcontext);
        System.out.println("Document Content  :" + handler.toString());

        System.out.println("Document Metadata :");
        String[] metadataNames = metadata.names();

        this.videoName = FilenameUtils.removeExtension(file);

        for(String name : metadataNames) {
            System.out.println(name + ": " + metadata.get(name));
            if(name=="tiff:ImageLength")
                this.frameHeight = metadata.get(name);
            if(name=="tiff:ImageWidth")
                this.frameWidth = metadata.get(name);
            if(name=="Creation-Date")
                this.dateCreated = metadata.get(name);
            if(name=="xmpDM:duration")
                this.length = metadata.get(name);
            if(name=="xmpDM:audioSampleRate")
                this.framerate=metadata.get(name);

        }


}


//this method splits a video to byte arrays and then adds the arrays to a list
    void SplitToChunks() throws IOException{
        File f = new File(this.file);
        FileInputStream inputstream = new FileInputStream(f);
        int n = 0;

        while ( n != -1){
             n = inputstream.read(videoFileChunk,0,KILOBYTE);
            ChunksList.add(videoFileChunk);

        }
        inputstream.close();

    }


    public String getDateCreated() {
        return dateCreated;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getFrameHeight() {
        return frameHeight;
    }

    public String getFrameWidth() {
        return frameWidth;
    }

    public String getFramerate() {
        return framerate;
    }

    public String getLength() {
        return length;
    }

    public String getVideoName() {
        return videoName;
    }

    public String getFile() {
        return file;
    }

    public ArrayList<byte[]> getChunksList() {
        return ChunksList;
    }

    public static void main(String[] args) throws IOException,SAXException, TikaException {
        VideoFile v = new VideoFile("mp4files/EarthExample.mp4");

        //this is for testing
/**
        v.getData("EarthExample.mp4");
        System.out.println("---------------------");
        System.out.println(v.dateCreated);
        System.out.println(v.frameHeight);
        System.out.println(v.length);
        System.out.println(v.frameWidth);
        System.out.println(v.framerate);
        System.out.println(v.videoName);
        v.associatedHashtags.add("#earth");
        v.associatedHashtags.add("#nature");
        v.associatedHashtags.add("#space");

        v.SplitToChunks();
        System.out.println(v.ChunksList.size());*/



    }



}

