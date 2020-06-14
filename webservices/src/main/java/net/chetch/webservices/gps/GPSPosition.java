package net.chetch.webservices.gps;
import java.util.Calendar;
import net.chetch.webservices.DataObject;

public class GPSPosition extends DataObject {
    public double getLatitude() {
        return getDouble("latitude");
    }

    public double getLongitude() {
        return getDouble("longitude");
    }

    public Calendar getUpdated() {
        return getCalendar("updated");
    }
}
