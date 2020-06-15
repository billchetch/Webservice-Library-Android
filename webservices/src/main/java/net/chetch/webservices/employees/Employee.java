package net.chetch.webservices.employees;

import android.graphics.Bitmap;
import android.media.Image;

import net.chetch.webservices.DataObject;
import net.chetch.webservices.DataObjectCollection;

public class Employee extends DataObject {

    public transient Bitmap profileImage;

    public String getEmployeeID(){
        return getString("employee_id");
    }

    public String getKnownAs(){
        return getString("known_as");
    }

    public String getFullName(){
        return getString("full_name");
    }

    @Override
    public Object getCasted(String fieldName){
        switch(fieldName){
            case "position_id":
            case "active":
                return getInteger(fieldName);

            default:
                return super.getCasted(fieldName);
        }
    }
}
