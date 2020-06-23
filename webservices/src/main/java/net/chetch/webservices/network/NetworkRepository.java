package net.chetch.webservices.network;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

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


    public LiveData<Services> getServices(){
        final MutableLiveData<Services> services = new MutableLiveData<>();

        if(service != null) {
            service.getServices().enqueue(createCallback(services));
        }
        return services;
    }
}
