package net.chetch.webservices.employees;

import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;

import net.chetch.webservices.DataObject;
import net.chetch.webservices.DataObjectCollection;

public class Employee extends DataObject {

    public transient Bitmap profileImage;

    public String getEmployeeID(){
        return getValue("employee_id").toString();
    }

    public String getKnownAs(){
        return getCasted("known_as");
    }

    public String getFullName(){
        return getCasted("full_name");
    }

    public void setActive(boolean active){
        setValue("active", active ? 1 : 0);
    }

    public boolean isActive(){
        return this.<Integer>getCasted("active") == 1;
    }



    @Override
    public boolean read(DataObject dataObject) {
        boolean success = super.read(dataObject);
        if(success){
            profileImage = ((Employee)dataObject).profileImage;
        }
        return success;
    }
}
