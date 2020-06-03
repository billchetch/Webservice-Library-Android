package net.chetch.webservices.gps;
import java.util.Calendar;
import net.chetch.webservices.DataObject;

public class GPSPosition extends DataObject {
    private double latitude;
    private double longitude;
    private Calendar updated;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Calendar getUpdated() {
        return updated;
    }
}
