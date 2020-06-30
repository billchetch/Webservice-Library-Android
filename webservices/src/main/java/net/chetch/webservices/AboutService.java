package net.chetch.webservices;

import java.util.Calendar;
import java.util.HashMap;

public class AboutService extends DataObject {

    public AboutService(){
        super();
    }

    public Calendar getServerTime(){
        return getCasted("server_time");
    }

    public String getVersion(){
        return getCasted("api_version");
    }

}
