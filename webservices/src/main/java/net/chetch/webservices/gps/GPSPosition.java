package net.chetch.webservices.gps;
import java.util.Calendar;

import net.chetch.utilities.Utils;
import net.chetch.webservices.DataObject;

public class GPSPosition extends DataObject {

    public enum SpeedUnits{
        KPH,
        MPH,
        NPH,
        MPS
    }
    @Override
    public void init() {
        super.init();

        asDouble("latitude", "longitude" ,"speed_mps");
    }

    public double getLatitude() {
        return getCasted("latitude");
    }

    public double getLongitude() {
        return getCasted("longitude");
    }

    public Double getSpeed(SpeedUnits speedUnit) {
        Double speedMPS = getCasted("speed_mps");
        Double speed = null;
        switch(speedUnit){
            case KPH:
                speed = (speedMPS * 3600) / 1000;
                break;

            case MPH:
                speed = (speedMPS * 3600 * 0.6) / 3600;
                break;

            case NPH:
                speed = (speedMPS * 3600 * 0.54) / 3600;
                break;

            default:
                speed = speedMPS;
                break;
        }

        return speed;
    }

    public Integer getBearing(){
        return getCasted("bearing");
    }

    public String getCompassHeading(){
        Integer bearing = getBearing();
        return Utils.convert(bearing, Utils.Conversions.DEG_2_COMPASS);
    }

    public Calendar getUpdated() {
        return getCasted("updated");
    }

}
