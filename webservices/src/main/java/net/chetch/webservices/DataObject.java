package net.chetch.webservices;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Calendar;

abstract public class DataObject extends HashMap<String, String> {

    private transient HashMap<String, String> oldValues = new HashMap<>();

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
        return get(fieldName);
    }

    public Calendar getCalendar(String fieldName, String dateFormat){
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
        oldValues.put(fieldName, get(fieldName));
        put(fieldName, fieldValue.toString());
    }

    public Integer getID(){ return (Integer)getCasted("id"); }

    public boolean isDirty(){
        return getID() == 0 || oldValues.size() > 0;
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
}
