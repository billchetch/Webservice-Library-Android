package net.chetch.webservices;

import java.util.Calendar;
import java.util.HashMap;

public class AboutService extends DataObject {

    public AboutService(){
        super();
    }

    public Calendar getServerTime(String dateFormat){
        return getCalendar("server_time", dateFormat);
    }

    public Calendar getServerTime(){
        return getServerTime(Webservice.DEFAULT_DATE_FORMAT);
    }

    public String getVersion(){
        return getString("api_version");
    }

}
