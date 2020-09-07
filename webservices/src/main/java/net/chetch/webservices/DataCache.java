package net.chetch.webservices;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public class CacheEntry<T> extends DataStore<T>{
        final public String key;
        private int cacheTime;
        private long dataLastUpdated;
        private boolean waitingForData = false;

        CacheEntry(String key, int cacheTime){
            this.key = key;
            this.cacheTime = cacheTime;
            this.dataLastUpdated = -1;
        }

        public boolean hasExpired(){
            boolean expired = false;

            if(cacheTime == NO_CACHE || isEmpty()) {
                expired = true;
            } else if(cacheTime == FOREVER_CACHE){
                expired = false;
            } else if(!isEmpty()){
                expired = System.currentTimeMillis() > dataLastUpdated + (cacheTime*1000);
            }

            return expired;
        }

        public boolean requiresUpdating(){
            if(hasExpired() && !waitingForData){
                waitingForData = true;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void updateData(Object newValue){
            super.updateData(newValue);
            waitingForData = false;
            dataLastUpdated = System.currentTimeMillis();

            Log.i("CacheEntry", "Updated value for " + key);
        }


        public void forceExpire(){
            this.waitingForData = false;
            this.dataLastUpdated = -1;
        }

        @Override
        public DataStore<T> add(MutableLiveData<T> liveData){
            //if the cache data is still fresh and it has a value then we trigger the live data upon adding
            //if the cache data has expired then the live data object will be set when the cache is updated
            //with a fresh value
            if(!hasExpired() && !isEmpty()){
                liveData.postValue(getData());
            }
            super.add(liveData);

            return this;
        }


        public DataStore<T> observe(Observer<T> observer){
            if(!hasObserver(observer)){
                boolean add2list = !isTemporaryObserver(observer);
                if(!hasExpired() && !waitingForData){
                    observer.onChanged(getData());
                } else {
                    add2list = true;
                }

                if(add2list){
                    super.observe(observer);
                }
            }
            return this;
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

    public <T> CacheEntry<T> getCacheEntry(String key, ITypeConverter<T> typeConverter, int cacheTime){
        CacheEntry<T> cacheEntry;
        if(cache.containsKey(key)){
            cacheEntry = cache.get(key);
        } else {
            cacheEntry = new CacheEntry<T>(key, cacheTime);
            cache.put(key, cacheEntry);
        }
        if(typeConverter != null){
            cacheEntry.setTypeConverter(typeConverter);
        }
        return cacheEntry;
    }

    public <T> CacheEntry<T> getCacheEntry(String key){
        return this.<T>getCacheEntry(key, null, defaultCacheTime);
    }

    public <T> CacheEntry<T> getCacheEntry(String key, ITypeConverter<T> typeConverter){
        return this.<T>getCacheEntry(key, typeConverter, defaultCacheTime);
    }

    public boolean isCacheEntry(String key){
        return cache.containsKey(key);
    }

    public boolean entryIsEmpty(String key){
        if(isCacheEntry(key)) {
            return cache.get(key).isEmpty();
        } else {
            return true;
        }
    }

    public void forceExpire(List<String> keys, boolean include){
        for(Map.Entry <String, CacheEntry> entry : cache.entrySet()){
            if(keys == null || (keys.contains(entry.getKey()) ? include : !include)){
                entry.getValue().forceExpire();
            }
        }
    }

    public boolean forceExpire(String key){
        if(cache.containsKey(key)){
            cache.get(key).forceExpire();
            return true;
        } else {
            return false;
        }
    }

    public void forceExpire(String ... keys){
        forceExpire(Arrays.asList(keys), true);
    }

    public void forceExpire(){
        forceExpire(null, true);
    }
}
