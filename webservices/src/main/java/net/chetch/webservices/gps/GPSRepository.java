package net.chetch.webservices.gps;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import net.chetch.webservices.DataCache;
import net.chetch.webservices.DataStore;
import net.chetch.webservices.Webservice;
import net.chetch.webservices.WebserviceRepository;
import net.chetch.webservices.employees.EmployeesRepository;
import net.chetch.webservices.network.Services;

import java.util.Calendar;

public class GPSRepository extends WebserviceRepository<IGPSService>{

    static private GPSRepository instance = null;
    static public GPSRepository getInstance(){
        if(instance == null)instance = new GPSRepository();
        return instance;
    }

    public GPSRepository() {
        super(new Webservice(IGPSService.class));

        setDefaultCacheTime(DataCache.VERY_SHORT_CACHE);
    }

    public DataStore<GPSPosition> getLatestPosition(){
        DataCache.CacheEntry<GPSPosition> entry = cache.getCacheEntry("latest-position");

        if(entry.requiresUpdating()) {
            service.getLatestPosition().enqueue(createCallback(entry));
        }

        return entry;
    }
}
