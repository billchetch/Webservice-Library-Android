package net.chetch.webservices;

import java.util.HashMap;

abstract public class DataObject extends HashMap<String, String> {

    private transient HashMap<String, String> oldValues = new HashMap<>();

    public int getInt(String fieldName){
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
        return get(fieldName).toString();
    }

    public void set(String fieldName, Object fieldValue){
        oldValues.put(fieldName, get(fieldName));
        put(fieldName, fieldValue.toString());
    }

    public int getID(){ return getInt("id"); }

    public boolean isDirty(){
        return getID() == 0 || oldValues.size() > 0;
    }

}
