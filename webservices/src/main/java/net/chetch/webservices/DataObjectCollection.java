package net.chetch.webservices;


import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class DataObjectCollection<D extends DataObject> extends ArrayList<D> {

    public class FilterCriteria extends HashMap<String, Object>{

        public HashMap<String, Field> fields2match = new HashMap<>();

        public FilterCriteria(String fieldName, Object fieldValue){
            super();
            put(fieldName, fieldValue);
        }

        public FilterCriteria(FilterCriteria criteria){
            for(Map.Entry<String, Object> entry : criteria.entrySet()){
                put(entry.getKey(), entry.getValue());
            }
        }

        public FilterCriteria(FilterCriteria criteria, D dataObject, Field[] fields){
            this(criteria);

            for(Field field : fields){
                if(containsKey(field.getName())){
                    fields2match.put(field.getName(), field);
                }
            }
        }

        public boolean matches(D dataObject){
            boolean matches = true;
            for(Map.Entry<String, Object> entry :entrySet()){
                try {
                    Object value1 = entry.getValue();
                    Field field = fields2match.get(entry.getKey());
                    Object value2 = field.get(dataObject);

                    if(value1 != null && value2 == null){
                        return false;
                    } else if(!value1.equals(value2)){
                        return false;
                    }
                } catch(IllegalAccessException e){
                    Log.e("DataObjectCollection", "Cannot access " + entry.getKey());
                    return false;
                }
            }
            return matches;
        }
    } //end of FilterCriteria class


    public class FieldMap<T> extends HashMap<T, D>{

    }

    public class IDMap extends FieldMap<Integer>{

    }

    Field[] fields = null;

    protected void setFields(D dataObject){
        fields = dataObject.getClass().getDeclaredFields();
        for(Field field : fields){
            field.setAccessible(true);
        }
    }

    public DataObjectCollection<D> populateFilterResults(DataObjectCollection<D> filtered, List<FilterCriteria> criteria){
        List<FilterCriteria> criteria2match = null;
        for(D dataObject : this){
            if(fields == null){
                setFields(dataObject);
            }
            if(criteria2match == null){
                criteria2match = new ArrayList<>();
                for(FilterCriteria fc : criteria) {
                    criteria2match.add(new FilterCriteria(fc, dataObject, fields));
                }
            }

            for(FilterCriteria c2m : criteria2match){
                if(c2m.matches(dataObject)) {
                    filtered.add(dataObject);
                    break;
                }
            }
        }
        return filtered;
    }

    public DataObjectCollection<D> populateFilterResults(DataObjectCollection<D> filtered, FilterCriteria criteria){
        List<FilterCriteria> fcs = new ArrayList<>();
        fcs.add(criteria);
        return populateFilterResults(filtered, fcs);
    }

    public DataObjectCollection<D> populateFilterResults(DataObjectCollection<D> filtered, String fieldName, Object fieldValue){
        return populateFilterResults(filtered, new FilterCriteria(fieldName, fieldValue));
    }

    public DataObjectCollection<D> filter(List<FilterCriteria> criteria){
        DataObjectCollection<D> dataObjectCollection = new DataObjectCollection<>();
        populateFilterResults(dataObjectCollection, criteria);
        return dataObjectCollection;
    }

    public DataObjectCollection<D> filter(FilterCriteria criteria){
        List<FilterCriteria> fcs = new ArrayList<>();
        fcs.add(criteria);
        return filter(fcs);
    }

    public DataObjectCollection<D> filter(String fieldName, Object fieldValue){
        return filter(new FilterCriteria(fieldName, fieldValue));
    }


    private <T> void populateFieldMap(FieldMap<T> fieldMap, String fieldName) throws Exception{
        Field keyField = null;

        for(D dataObject : this){
            if(fields == null){
                setFields(dataObject);
            }

            if(keyField == null){
                for(Field field : fields){
                    if(field.getName().equals(fieldName)){
                        keyField = field;
                        break;
                    }
                }
                if(keyField == null)throw new Exception("No field matches " + fieldName);
            }

            fieldMap.put((T)keyField.get(dataObject), dataObject);
        }
    }

    public <T> FieldMap<T> asFieldMap(String fieldName) throws Exception{
        FieldMap<T> fieldMap = new FieldMap<>();
        populateFieldMap(fieldMap, fieldName);
        return fieldMap;
    }

    public IDMap asIDMap(){
        try {
            IDMap idMap = new IDMap();
            populateFieldMap(idMap, "id");
            return idMap;
        } catch (Exception e){
            //no need to do anything
            return null;
        }
    }
}
