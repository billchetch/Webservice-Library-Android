package net.chetch.webservices;


import android.util.Log;

import net.chetch.utilities.SLog;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

abstract public class DataObjectCollection<D extends DataObject> extends ArrayList<D> {

    public class FilterCriteria extends LinkedHashMap<String, Object>{

        public FilterCriteria(){
            super();
        }

        public FilterCriteria(String fieldName, Object fieldValue){
            super();
            put(fieldName, fieldValue);
        }


        public boolean matches(DataObject dataObject){
            for(Map.Entry<String, Object> entry : entrySet()){
                try {
                    String fieldName = entry.getKey();
                    Object value = entry.getValue();
                    if(!dataObject.equals(fieldName, value)){
                        return false;
                    }
                } catch(Exception e){
                    if(SLog.LOG)SLog.e("DataObjectCollection", "Matches exception on " + entry.getKey() + " " + e.getMessage());
                    return false;
                }
            }
            return true;
        }
    } //end of FilterCriteria class

    public enum SortOptions{
        ASC,
        DESC
    }

    public class SortOn extends LinkedHashMap<String, SortOptions> implements Comparator<DataObject> {
        public boolean nullIsLess = true;

        @Override
        public int compare(DataObject dataObject1, DataObject dataObject2){
            int comparison = 0;

            for(Map.Entry<String, SortOptions> entry : entrySet()){
                SortOptions sortOptions = entry.getValue();
                String fieldName = entry.getKey();

                try {
                    comparison = dataObject1.compare(fieldName, dataObject2.getComparable(fieldName), nullIsLess);
                    if(comparison != 0){
                        comparison = comparison*(sortOptions == SortOptions.ASC ? 1 : -1);
                        break;
                    }

                } catch (Exception e){
                    if(SLog.LOG)SLog.e("DataObjectCollection", "SortOn::compare exception: " + e.getMessage() + " field " + fieldName);
                }
            }
            return comparison;
        }
    } //end SortOn class

    public class FieldMap<T> extends HashMap<T, D>{

    }

    public class IDMap extends FieldMap<Integer>{

    }

    //instance methods and fields
    protected Class collectionClass;
    protected Class itemClass;

    public <C extends DataObjectCollection> DataObjectCollection(Class<C> ccls, Class<D> icls){
        collectionClass = ccls;
        itemClass = icls;
    }

    public <C extends DataObjectCollection> DataObjectCollection(Class<C> ccls){
        this(ccls, null);
    }

    @Override
    public boolean equals(Object v){
        if(v == null)return false;

        if(!(v instanceof DataObjectCollection)){
            return false;
        }
        DataObjectCollection doc = (DataObjectCollection)v;
        if(doc.size() != size())return false;

        for(Object dataObject1 : this){
            boolean exists = false;
            for(Object dataObject2 : doc){
                if(dataObject1.equals(dataObject2)){
                    exists = true;
                    break;
                }
            }
            if(!exists)return false;
        }
        return true;
    }

    public boolean read(DataObjectCollection<?> dataObjectCollection){
        if(!dataObjectCollection.getClass().isAssignableFrom(getClass())){
            return false;
        }

        if(dataObjectCollection == null)return true;

        for(DataObject dataObject : dataObjectCollection){
            D newDataObject = createDataObject();
            if(newDataObject.read(dataObject)){
                add(newDataObject);
            }
        }

        return true;
    }

    protected <C extends DataObjectCollection> C createCollection(){
        try {
            C newCollection = (C)collectionClass.getDeclaredConstructor().newInstance();
            return newCollection;
        } catch (Exception e){
            return null;
        }
    }

    protected D createDataObject(){
        try {
            return itemClass == null ? null : (D)itemClass.getDeclaredConstructor().newInstance();
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

    public void populateFilterResults(DataObjectCollection filtered, List<FilterCriteria> criteria, boolean include){
        for(DataObject dataObject : this){
            boolean matched = false;
            for(FilterCriteria fc : criteria) {
                if(fc.matches(dataObject)){
                    matched = true;
                    if(include)break;
                }
            }
            if((matched && include) || (!matched && !include)){
                filtered.<D>add(dataObject);
            }
        }

    }

    public void populateFilterResults(DataObjectCollection filtered, FilterCriteria criteria, boolean include){
        List<FilterCriteria> fcs = new ArrayList<>();
        fcs.add(criteria);
        populateFilterResults(filtered, fcs, include);
    }

    public void populateFilterResults(DataObjectCollection filtered, String fieldName, Object fieldValue, boolean include){
        populateFilterResults(filtered, new FilterCriteria(fieldName, fieldValue), include);
    }


    public <C extends DataObjectCollection> C filter(List<FilterCriteria> criteria, boolean include){
        C dataObjectCollection = createCollection();
        populateFilterResults(dataObjectCollection, criteria, include);
        return dataObjectCollection;
    }

    public <C extends DataObjectCollection> C filter(List<FilterCriteria> criteria){
        return filter(criteria, true);
    }

    public  <C extends DataObjectCollection> C filter(FilterCriteria criteria, boolean include){
        List<FilterCriteria> fcs = new ArrayList<>();
        fcs.add(criteria);
        return filter(fcs, include);
    }

    public  <C extends DataObjectCollection> C filter(FilterCriteria criteria) {
        return filter(criteria, true);
    }

    public <C extends DataObjectCollection> C filter(String fieldName, Object[] fieldValues, boolean include) {
        List<FilterCriteria> criteria = new ArrayList<>();
        for(Object fieldValue : fieldValues){
            criteria.add(new FilterCriteria(fieldName, fieldValue));
        }

        return filter(criteria, include);
    }

    public <C extends DataObjectCollection> C filter(String fieldName, Object ... fieldValues){
        return filter(fieldName, fieldValues,true);
    }

    public <C extends DataObjectCollection> C exclude(String fieldName, Object ... fieldValues){
        return filter(fieldName, fieldValues,false);
    }

    public <C extends DataObjectCollection> C ids(Integer ... idValues){
        return filter("id", idValues,true);
    }

    public <C extends DataObjectCollection> C xids(Integer ... idValues){
        return filter("id", idValues,false);
    }

    public <C extends DataObjectCollection> C dirty() {
        C dataObjectCollection = createCollection();
        for(D dataObject : this){
            if(dataObject.isDirty()){
                dataObjectCollection.add(dataObject);
            }
        }
        return dataObjectCollection;
    }

    private <T> void populateFieldMap(FieldMap<T> fieldMap, String fieldName) throws Exception{
        for(D dataObject : this){
            fieldMap.put((T)dataObject.getValue(fieldName), dataObject);
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


    public <C extends DataObjectCollection> C sort(String fieldName, SortOptions sortOption){
        SortOn sortOn = new SortOn();
        sortOn.put(fieldName, sortOption);
        return sort(sortOn);
    }

    public <C extends DataObjectCollection> C sort(SortOn sortOn){
        if(size() > 0) {
            Collections.sort(this, sortOn);
        }
        return (C)this;
    }

    public <C extends DataObjectCollection> C limit(int start, int size){
        C limited = createCollection();

        for(int i = start; i < start + Math.min(size(), start + size); i++){
            limited.add(get(i));
        }

        return limited;
    }

    public <C extends DataObjectCollection> C limit(int size){
        return limit(0, size);
    }

    public D get(String fieldName, Object fieldValue){
        DataObjectCollection<D> filtered = filter(fieldName, fieldValue);
        if(filtered.size() > 0){
            return filtered.get(0);
        } else {
            return null;
        }
    }

    public D id(int id){
        return get("id", id);
    }
}
