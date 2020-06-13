package net.chetch.webservices;


import android.util.Log;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

abstract public class DataObjectCollection<D extends DataObject> extends ArrayList<D> {

    public class FilterCriteria extends HashMap<String, Object>{

        public HashMap<String, Field> fields2match = new HashMap<>();

        public FilterCriteria(){
            super();
        }

        public FilterCriteria(String fieldName, Object fieldValue){
            super();
            put(fieldName, fieldValue);
        }

        public FilterCriteria(FilterCriteria criteria){
            for(Map.Entry<String, Object> entry : criteria.entrySet()){
                put(entry.getKey(), entry.getValue());
            }
        }

        public FilterCriteria(FilterCriteria criteria, D dataObject, List<Field> fields){
            this(criteria);

            for(Field field : fields){
                if(containsKey(field.getName())){
                    fields2match.put(field.getName(), field);
                }
            }
        }

        public boolean matches(D dataObject){
            boolean matches = true;
            for(Map.Entry<String, Object> entry : entrySet()){
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

    public enum SortOptions{
        ASC,
        DESC
    }

    public class SortOn extends HashMap<String, SortOptions> implements Comparator<D> {
        private HashMap<String, Field> fields2Compare = new HashMap<>();

        public void setFieldsToCompareOn(List<Field> fields){
            for(Map.Entry<String, SortOptions> entry : entrySet()){
                String fieldName = entry.getKey();
                for(Field field : fields){
                    if(field.getName().equals(fieldName)){
                        field.setAccessible(true);
                        fields2Compare.put(fieldName, field);
                    }
                }
            }
        }

        @Override
        public int compare(D dataObject1, D dataObject2){
            int comparison = 0;

            for(Map.Entry<String, SortOptions> entry : entrySet()){
                SortOptions sortOptions = entry.getValue();
                String fieldName = entry.getKey();
                Field field = fields2Compare.get(fieldName);

                try {
                    Comparable v1 = (Comparable)field.get(dataObject1);
                    Comparable v2 = (Comparable)field.get(dataObject2);

                    if(v1 == null && v2 == null) {
                        comparison = 0;
                    } else if(v1 == null) {
                        comparison = -1;
                    } else if(v2 == null){
                        comparison = 1;
                    } else {
                        comparison = v1.compareTo(v2);
                    }

                    Log.i("Sort", "Comparing field " + fieldName + " on " + v1 + "," + v2 + " gives " + comparison);
                    if(comparison != 0){
                        comparison = comparison*(sortOptions == SortOptions.ASC ? 1 : -1);
                        break;
                    }

                } catch (Exception e){
                    Log.e("DataObjectCollection", "SortOn::compare exception: " + e.getMessage() + " field " + fieldName);
                }
            }
            return comparison;
        }
    } //end SortOn class

    public class FieldMap<T> extends HashMap<T, D>{

    }

    public class IDMap extends FieldMap<Integer>{

    }

    //instance methods and firelds
    Class collectionClass;
    List<Field> fields = null;

    public <C extends DataObjectCollection> DataObjectCollection(Class<C> cls){
        collectionClass = cls;
    }

    protected <C extends DataObjectCollection<D>> C createCollection(){
        try {
            C newCollection = (C)collectionClass.getDeclaredConstructor().newInstance();
            return newCollection;
        } catch (Exception e){
            return null;
        }
    }

    public FilterCriteria createFilter(){
        return new FilterCriteria();
    }

    public SortOn createSortOn(){
        return new SortOn();
    }

    protected void setFields(D dataObject){
        List<Class> classes = new ArrayList<>();

        Class c = dataObject.getClass();
        do{
            classes.add(c);
            c = c.getSuperclass();
        } while(DataObject.class.isAssignableFrom(c));


        fields = new ArrayList<>();
        for(Class cls : classes) {
            Field[] fields2add = cls.getDeclaredFields();
            for (Field field : fields2add) {
                if(!fields.contains(field)) {
                    field.setAccessible(true);
                    fields.add(field);
                }
            }
        }
    }

    public void populateFilterResults(DataObjectCollection<D> filtered, List<FilterCriteria> criteria){
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
    }

    public void populateFilterResults(DataObjectCollection<D> filtered, FilterCriteria criteria){
        List<FilterCriteria> fcs = new ArrayList<>();
        fcs.add(criteria);
        populateFilterResults(filtered, fcs);
    }

    public void populateFilterResults(DataObjectCollection<D> filtered, String fieldName, Object fieldValue){
        populateFilterResults(filtered, new FilterCriteria(fieldName, fieldValue));
    }

    public <C extends DataObjectCollection<D>> C filter(List<FilterCriteria> criteria){
        C dataObjectCollection = createCollection();
        populateFilterResults(dataObjectCollection, criteria);
        return dataObjectCollection;
    }

    public  <C extends DataObjectCollection<D>> C filter(FilterCriteria criteria){
        List<FilterCriteria> fcs = new ArrayList<>();
        fcs.add(criteria);
        return filter(fcs);
    }

    public <C extends DataObjectCollection<D>> C filter(String fieldName, Object fieldValue){
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


    public <C extends DataObjectCollection<D>> C sort(String fieldName, SortOptions sortOption){
        SortOn sortOn = new SortOn();
        sortOn.put(fieldName, sortOption);
        return sort(sortOn);
    }

    public <C extends DataObjectCollection<D>> C sort(SortOn sortOn){
        if(size() == 0)return (C)this;

        if(fields == null){
            setFields(get(0));
        }

        sortOn.setFieldsToCompareOn(fields);

        Collections.sort(this, sortOn);

        return (C)this;
    }

    public <C extends DataObjectCollection<D>> C limit(int start, int size){
        C limited = createCollection();

        for(int i = start; i < start + Math.max(size(), start + size); i++){
            limited.add(get(i));
        }
        return limited;
    }

    public <C extends DataObjectCollection<D>> C limit(int size){
        return limit(0, size);
    }
}
