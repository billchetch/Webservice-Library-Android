package net.chetch.webservices;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import android.util.Log;

import net.chetch.utilities.SLog;

import java.util.ArrayList;
import java.util.List;

public class DataStore<T> {

    protected ITypeConverter<T> typeConverter = null;
    private T data;

    private List<Observer> observers = new ArrayList<>();
    private List<MutableLiveData> liveDataList = new ArrayList<>();

    private boolean hasData = false;

    public void setTypeConverter(ITypeConverter<T> c){
        typeConverter = c;
    }

    public void updateData(Object newData) {
        if(typeConverter != null) {
            data = typeConverter.convert(newData);
        } else {
            data = (T) newData;
        }
        hasData = true;
        notifyObservers();
    }

    public T getData(){
        return data;
    }

    public boolean isEmpty(){
        return !hasData;
    }

    protected boolean isTemporaryObserver(Observer observer){
        return observer.getClass().isSynthetic();
    }

    public void notifyObservers(){
        //loop through observers and notify as well as record those that are temporary for removal
        //after notification.
        List<Observer> temporary = new ArrayList<>();
        for (Observer observer : observers) {
            try {
                observer.onChanged(data);
                if (isTemporaryObserver(observer)) {
                    temporary.add(observer);
                }
            } catch (Exception e){
                if(SLog.LOG)SLog.e("DataStore", "notifyObservers: " + e.getMessage() + (data == null ? "" : ", data: " + data.getClass()));
                e.printStackTrace();
            }
        }

        //remove the temporary observers
        for(Observer tempObserver : temporary){
            unobserve(tempObserver);
        }

        //set the value of the observing live data objects
        for (MutableLiveData liveData : liveDataList) {
            liveData.postValue(data);
        }
    }

    public DataStore<T> add(MutableLiveData<T> liveData){
        if(!hasLiveData(liveData)){
            liveDataList.add(liveData);
        }
        return this;
    }

    public void remove(MutableLiveData liveData){
        if(hasLiveData(liveData)){
            liveDataList.remove(liveData);
        }
    }

    public boolean hasLiveData(MutableLiveData liveData){
        return liveDataList.contains(liveData);
    }

    public DataStore<T> observe(Observer<T> observer){
        if(!hasObserver(observer)){
            observers.add(observer);
        }
        return this;
    }

    public void unobserve(Observer observer){
        if(hasObserver(observer)){
            observers.remove(observer);
        }
    }

    public boolean hasObserver(Observer observer){
        return observers.contains(observer);
    }
}
