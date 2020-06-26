package net.chetch.webservices;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import net.chetch.webservices.employees.Employees;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

public class DataCache {
    static public int NO_CACHE = 0;
    static public int VERY_SHORT_CACHE = 5;
    static public int SHORT_CACHE = 30;
    static public int MEDIUM_CACHE = 5*60;
    static public int LONG_CACHE = 30*60;
    static public int VERY_LONG_CACHE = 6*60*60;
    static public int FOREVER_CACHE = -1;

    protected int defaultCacheTime = NO_CACHE;
    private HashMap<String, CacheEntry> cache = new HashMap<>();


    public class CacheEntry{
        final public String key;
        private List<Observer> observers = new ArrayList<>();
        private List<MutableLiveData> liveDataList = new ArrayList<>();
        private int cacheTime;
        private long valueLastUpdated;
        private boolean hasValue = false;
        private Object data;

        CacheEntry(String key, int cacheTime){
            this.key = key;
            this.cacheTime = cacheTime;
            this.valueLastUpdated = -1;
            this.hasValue = false;
        }

        public boolean hasExpired(){
            boolean expired = false;

            if(cacheTime == NO_CACHE || valueLastUpdated == -1 || !hasValue) {
                expired = true;
                valueLastUpdated = 0;
            }else if(cacheTime == FOREVER_CACHE){
                expired = false;
            } else if(valueLastUpdated > 0){
                expired = System.currentTimeMillis() > valueLastUpdated + (cacheTime*1000);
            }

            return expired;
        }

        public void notifyObservers(boolean forceNotify){
            if(!hasExpired() || forceNotify) {
                for (Observer observer : observers) {
                    observer.onChanged(data);
                }
                for (MutableLiveData liveData : liveDataList) {
                    liveData.setValue(data);
                }
            }
        }

        public void notifyObservers(){
            notifyObservers(false);
        }

        public void updateValue(Object newValue){
            data = newValue;
            hasValue = true;
            valueLastUpdated = System.currentTimeMillis();

            notifyObservers(true);
            Log.i("CacheEntry", "Updated value for " + key);
        }


        public void expireOnNextCall(){
            this.valueLastUpdated = -1;
        }

        public <T> LiveData<T> add(MutableLiveData<T> liveData){
            if(!liveDataList.contains(liveData)){
                if(!hasExpired()){
                    liveData.setValue((T)data);
                }
                liveDataList.add(liveData);
            }
            return liveData;
        }

        public LiveData remove(MutableLiveData liveData){
            if(liveDataList.contains(liveData)){
                liveDataList.remove(liveData);
            }
            return liveData;
        }

        public <T> CacheEntry observe(Observer<T> observer){
            if(!observers.contains(observer)){
                if(!hasExpired()){
                    observer.onChanged((T)data);
                }
                observers.add(observer);
            }
            return this;
        }

        public void unobserve(Observer observer){
            if(observers.contains(observer)){
                observers.remove(observer);
            }
        }
    }

    public DataCache(){

    }

    public DataCache(int defaultCacheTime){
        setDefaultCacheTime(defaultCacheTime);
    }

    public void setDefaultCacheTime(int cacheTime){
        this.defaultCacheTime = cacheTime;
    }

    public CacheEntry getCacheEntry(String key, int cacheTime){
        CacheEntry cacheEntry;
        if(cache.containsKey(key)){
            cacheEntry = cache.get(key);
        } else {
            cacheEntry = new CacheEntry(key, cacheTime);
            cache.put(key, cacheEntry);
        }
        return cacheEntry;
    }

    public CacheEntry getCacheEntry(String key){
        return this.getCacheEntry(key, defaultCacheTime);
    }

    public void setExpireOnNextCall(List<String> keys, boolean include){
        for(Map.Entry <String, CacheEntry> entry : cache.entrySet()){
            if(keys == null || (keys.contains(entry.getKey()) ? include : !include)){
                entry.getValue().expireOnNextCall();
            }
        }
    }

    public boolean setExpireOnNextCall(String key){
        if(cache.containsKey(key)){
            cache.get(key).expireOnNextCall();
            return true;
        } else {
            return false;
        }
    }

    public void setExpireOnNextCall(String ... keys){
        setExpireOnNextCall(Arrays.asList(keys), true);
    }

    public void setExpireOnNextCall(){
        setExpireOnNextCall(null, true);
    }
}
