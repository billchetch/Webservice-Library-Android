package net.chetch.webservices;

import android.provider.ContactsContract;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Calendar;

abstract public class DataObject extends HashMap<String, String> {

    private transient HashMap<String, Object> oldValues = new HashMap<>();

    public Integer getInteger(String fieldName){
        if(!containsKey(fieldName) || get(fieldName) == null){
            return 0;
        } else {
            return Integer.parseInt(get(fieldName));
        }
    }

    public double getDouble(String fieldName){
        if(!containsKey(fieldName) || get(fieldName) == null){
            return 0.0;
        } else {
            return Double.parseDouble(get(fieldName));
        }
    }

    public long getLong(String fieldName){
        if(!containsKey(fieldName) || get(fieldName) == null){
            return 0;
        } else {
            return Long.parseLong(get(fieldName));
        }
    }

    public String getString(String fieldName){
        if(!containsKey(fieldName)){
            return null;
        } else {
            return get(fieldName);
        }
    }

    public Calendar getCalendar(String fieldName, String dateFormat){
        if(!containsKey(fieldName)) {
            return null;
        }

        String dateString = get(fieldName);
        SimpleDateFormat f = new SimpleDateFormat(dateFormat);
        Calendar cal = Calendar.getInstance();
        try {
           cal.setTime(f.parse(dateString));
           return cal;
        } catch (Exception e) {
            return null;
        }
    }

    public Calendar getCalendar(String fieldName) {
        return getCalendar(fieldName, Webservice.DEFAULT_DATE_FORMAT);
    }

    public Object getCasted(String fieldName){
        switch(fieldName){
            case "id":
                return getInteger(fieldName);

            default:
                return getString(fieldName);
        }
    }

    public Comparable getComparable(String fieldName) {
        return (Comparable)getCasted(fieldName);
    }

    public void set(String fieldName, Object fieldValue){
        oldValues.put(fieldName, getCasted(fieldName));
        put(fieldName, fieldValue == null ? null : fieldValue.toString());
    }

    public void unset(String fieldName){
        oldValues.remove(fieldName);
        remove(fieldName);
    }

    public Integer getID(){ return (Integer)getCasted("id"); }

    public boolean isDirty(){
        if(getID() == 0) { //a client side created object
            return true;
        } else { //this originates from the server
            for(String fieldName : oldValues.keySet()){
                Object oldValue = oldValues.get(fieldName);
                if(oldValue == null){
                    return oldValue == get(fieldName);
                } else {
                    try {
                        return !equals(fieldName, oldValue);
                    } catch (Exception e){
                        return false;
                    }
                }
            }
            return false;
        }
    }

    public void clean(){
        oldValues.clear();
    }

    protected int compareNull(Object v1, Object v2, boolean nullIsLess) throws Exception {
        if(v1 == null && v2 == null){
            return 0;
        } else if(v1 == null){
            return nullIsLess ? -1 : 1;
        } else if(v2 == null){
            return nullIsLess ? 1 : -1;
        } else {
            throw new Exception("Neither value is null");
        }
    }

    public int compare(String fieldName, Comparable v2, boolean nullIsLess) throws Exception{
        Comparable v1 = getComparable(fieldName);
        if(v1 == null || v2 == null){
            return compareNull(v1, v2, nullIsLess);
        } else {
            return v1.compareTo(v2);
        }
    }

    public boolean equals(String fieldName, Object v2){
        Object v1 = getCasted(fieldName);

        if(v1 == null && v2 == null){
            return true;
        } else if(v1 == null){
            return v2.equals(v1);
        } else {
            return v1.equals(v2);
        }
    }

    public boolean equals(String fieldName, DataObject dataObject){
        return equals(fieldName, dataObject.getCasted(fieldName));
    }

    @Override
    public boolean equals(Object v){
        if(v == null)return false;
        if(!(v instanceof DataObject))return false;

        DataObject dataObject = (DataObject)v;

        if(size() != dataObject.size())return false;
        for(String fieldName : keySet()){
            if(!equals(fieldName, dataObject)){
                return false;
            }
        }
        return true;
    }

    public boolean read(DataObject dataObject){
        if(!dataObject.getClass().isAssignableFrom(getClass())){
            return false;
        }

        if(dataObject == null)return true;

        for(String fieldName : dataObject.keySet()){
            String newFieldValue = dataObject.get(fieldName);
            put(fieldName, newFieldValue);
            if(oldValues.containsKey(fieldName)){
                oldValues.remove(fieldName);
            }
        }

        return true;
    }
}
