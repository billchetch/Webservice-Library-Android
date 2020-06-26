package net.chetch.webservices.network;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import net.chetch.webservices.AboutService;
import net.chetch.webservices.DataCache;
import net.chetch.webservices.DataCache;
import net.chetch.webservices.Webservice;
import net.chetch.webservices.WebserviceRepository;
import net.chetch.webservices.employees.EmployeesRepository;
import net.chetch.webservices.gps.GPSRepository;

import java.util.HashMap;
import java.util.List;


public class NetworkRepository extends WebserviceRepository<INetworkService> {

    static private NetworkRepository instance = null;
    static public NetworkRepository getInstance(){
        if(instance == null)instance = new NetworkRepository();
        return instance;
    }

    public NetworkRepository() {
        super(new Webservice(INetworkService.class));
    }

    public DataCache.CacheEntry getServices(){
        DataCache.CacheEntry entry = cache.getCacheEntry("services");

        if(entry.hasExpired()) {
            service.getServices().enqueue(createCallback(entry));
        }
        return entry;
    }
}
