package net.chetch.webservices.gps;
import android.location.Location;

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

    public GPSPosition(){

    }

    public GPSPosition(android.location.Location location){
        setLatitude(location.getLatitude());
        setLongitude(location.getLongitude());
        if(location.hasBearing()){
            setValue("bearing", location.getBearing());
        }
        if(location.hasSpeed()){
            setValue("speed", location.getSpeed());
        }
    }

    @Override
    public void initialise() {
        super.initialise();

        asDouble("latitude", "longitude" ,"speed", "bearing");
    }

    public double getLatitude() {
        return getCasted("latitude");
    }
    public void setLatitude(double latitude){
        setValue("latitude", latitude);
    }


    public double getLongitude() {
        return getCasted("longitude");
    }
    public void setLongitude(double longitude){
        setValue("longitude", longitude);
    }

    public Double getSpeed(SpeedUnits speedUnit) {
        Double speedMPS = getCasted("speed");
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

    public Double getSpeed() {
        return getSpeed(SpeedUnits.MPS);
    }

    public Double getBearing(){
        return getCasted("bearing");
    }

    public String getCompassHeading(){
        double bearing = getBearing();
        return Utils.convert((int)bearing, Utils.Conversions.DEG_2_COMPASS);
    }

    public Calendar getUpdated() {
        return getCasted("updated");
    }

    public float distanceTo(GPSPosition pos){
        Location l1 = asLocation();
        Location l2 = pos.asLocation();
        return l1.distanceTo(l2);

    }

    public Location asLocation(){
        Location l = new Location("");
        l.setLatitude(getLatitude());
        l.setLongitude(getLongitude());
        return l;
    }
}
