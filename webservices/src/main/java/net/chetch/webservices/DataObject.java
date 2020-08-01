package net.chetch.webservices;

import android.provider.ContactsContract;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Calendar;
import java.util.Map;
import java.util.regex.Pattern;

abstract public class DataObject extends HashMap<String, DataField> {

    private transient HashMap<String, Object> oldValues = new HashMap<>();

    public DataObject(){

    }

    public void initialise(){
        for(String fieldName : keySet()){
            get(fieldName).setName(fieldName);
        }
    }

    public DataField getField(String fieldName){
        return containsKey(fieldName) ? get(fieldName) : null;
    }

    public boolean hasField(String fieldName){
        return containsKey(fieldName);
    }

    public <T> T getCasted(String fieldName){
        return getCasted(fieldName, null);
    }

    public <T> T getCasted(String fieldName, Object defaultValue){
        return (T)getValue(fieldName, defaultValue);
    }

    public Object getValue(String fieldName){
        return getValue(fieldName, null);
    }

    public Object getValue(String fieldName, Object defaultValue){
        DataField field = getField(fieldName);
        return field == null ? defaultValue : field.getValue();
    }

    public Comparable getComparable(String fieldName) {
        return (Comparable)getValue(fieldName);
    }


    public void setValue(String fieldName, Object fieldValue){
        DataField field;
        if(!hasField(fieldName)) {
            field = new DataField(fieldName);
        } else {
            field = getField(fieldName);
        }
        field.setValue(fieldValue);
        put(fieldName, field);
        oldValues.put(fieldName, fieldValue);
    }

    public void unset(String fieldName){
        oldValues.remove(fieldName);
        remove(fieldName);
    }

    protected void asEnum(String fieldName, Class<? extends Enum> enumClass){
        DataField field = getField(fieldName);
        if(field != null){
            Object value = field.getValue();
            if(value != null){
                field.setValue(Enum.valueOf(enumClass, value.toString()));
            }
        }
    }

    protected void asString(String fieldName){
        DataField field = getField(fieldName);
        if(field != null){
            Object value = field.getValue();
            if(value != null)field.setValue(value.toString());
        }
    }

    protected void asString(String ... fieldNames){
        for(String fieldName : fieldNames){
            asString(fieldName);
        }
    }

    protected void asInteger(String fieldName){
        DataField field = getField(fieldName);
        if(field != null){
            Object value = field.getValue();
            if(value != null)field.setValue(Integer.parseInt(value.toString()));
        }
    }

    protected void asLong(String fieldName){
        DataField field = getField(fieldName);
        if(field != null){
            Object value = field.getValue();
            if(value != null)field.setValue(Long.parseLong(value.toString()));
        }
    }

    protected void asDouble(String fieldName){
        DataField field = getField(fieldName);
        if(field != null){
            Object value = field.getValue();
            if(value != null)field.setValue(Double.parseDouble(value.toString()));
        }
    }

    protected void asDouble(String ... fieldNames){
        for(String fieldName : fieldNames){
            asDouble(fieldName);
        }
    }

    public boolean isNew(){
        return getID() == 0;
    }

    public Integer getID(){ return getCasted("id", 0); }

    public boolean isDirty(){
        if(getID() == 0) { //a client side created object
            return true;
        } else { //this originates from the server
            for(String fieldName : oldValues.keySet()){
                Object oldValue = oldValues.get(fieldName);
                if(oldValue == null){
                    return oldValue == getValue(fieldName);
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
        Object v1 = getValue(fieldName);

        if(v1 == null && v2 == null){
            return true;
        } else if(v1 == null){
            return v2.equals(v1);
        } else {
            return v1.equals(v2);
        }
    }

    public boolean equals(String fieldName, DataObject dataObject){
        return equals(fieldName, dataObject.getValue(fieldName));
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
            DataField field = dataObject.getField(fieldName);
            put(fieldName, field);
            if(oldValues.containsKey(fieldName)){
                oldValues.remove(fieldName);
            }
        }

        return true;
    }
}
