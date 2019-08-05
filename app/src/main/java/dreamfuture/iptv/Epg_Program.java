package dreamfuture.iptv;

/**
 * Created by STAR-Z on 2017-09-14.
 */

public class Epg_Program {
    public String start;
    public String stop;
    public String channel;
    public String title;
    public String description;
    public Epg_Program(String start, String stop, String channel, String title, String description){
        this.start = start;
        this.stop = stop;
        this.channel = channel;
        this.title = title;
        this.description = description;
    }
    public Epg_Program(){
        start = "";
        stop = "";
        channel = "";
        title = "";
        description = "";
    }
}
