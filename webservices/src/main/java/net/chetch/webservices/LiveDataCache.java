package net.chetch.webservices;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiveDataCache {
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
        public String key;
        public MutableLiveData liveData;
        public int cacheTime;
        public long valueLastUpdated;

        CacheEntry(String key, MutableLiveData liveData, int cacheTime){
            this.key = key;
            this.liveData = liveData;
            this.cacheTime = cacheTime;
            this.valueLastUpdated = -1;
        }

        public boolean refreshValue(){
            boolean refresh = false;

            if(cacheTime == NO_CACHE || valueLastUpdated == -1) {
                refresh = true;
                valueLastUpdated = 0;
            }else  if(cacheTime == FOREVER_CACHE){
                refresh = false;
            } else if(valueLastUpdated > 0){
                refresh = System.currentTimeMillis() > valueLastUpdated + (cacheTime*1000);
            }

            return refresh;
        }

        public void updateValue(Object newValue){
            liveData.setValue(newValue);
            valueLastUpdated = System.currentTimeMillis();
            Log.i("CacheEntry", "Updated value for " + key);
        }

        public void refreshOnNextCall(){
            this.valueLastUpdated = -1;
        }
    }

    public LiveDataCache(){

    }

    public LiveDataCache(int defaultCacheTime){
        setDefaultCacheTime(defaultCacheTime);
    }

    public void setDefaultCacheTime(int cacheTime){
        this.defaultCacheTime = cacheTime;
    }

    public <T> CacheEntry getCacheEntry(String key, int cacheTime){
        if(cache.containsKey(key)){
            return cache.get(key);
        } else {
            CacheEntry cacheEntry = new CacheEntry(key, new MutableLiveData<T>(), cacheTime);
            cache.put(key, cacheEntry);
            return cacheEntry;
        }
    }

    public <T> CacheEntry getCacheEntry(String key){
        return this.<T>getCacheEntry(key, defaultCacheTime);
    }

    public void setRefreshOnNextCall(List<String> keys, boolean include){
        for(Map.Entry <String, CacheEntry> entry : cache.entrySet()){
            if(keys == null || (keys.contains(entry.getKey()) ? include : !include)){
                entry.getValue().refreshOnNextCall();
            }
        }
    }

    public boolean setRefreshOnNextCall(String key){
        if(cache.containsKey(key)){
            cache.get(key).refreshOnNextCall();
            return true;
        } else {
            return false;
        }
    }

    public void setRefreshOnNextCall(String ... keys){
        setRefreshOnNextCall(Arrays.asList(keys), true);
    }

    public void refreshOnNextCall(){
        setRefreshOnNextCall(null, true);
    }
}
