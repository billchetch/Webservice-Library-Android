package net.chetch.webservices.gps;
import java.util.Calendar;
import net.chetch.webservices.DataObject;

public class GPSPosition extends DataObject {
    public double getLatitude() {
        return getCasted("latitude");
    }

    public double getLongitude() {
        return getCasted("longitude");
    }

    public Calendar getUpdated() {
        return getCasted("updated");
    }
}
