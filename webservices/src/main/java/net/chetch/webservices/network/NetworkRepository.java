package net.chetch.webservices.network;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import net.chetch.webservices.Webservice;
import net.chetch.webservices.WebserviceRepository;

import java.util.HashMap;
import java.util.List;


public class NetworkRepository extends WebserviceRepository<INetworkService> {

    public NetworkRepository() {
        super(new Webservice(INetworkService.class));
    }


    public LiveData<HashMap<String, Service>> getServices(){
        final MutableLiveData<HashMap<String, Service>> services = new MutableLiveData<>();

        if(service != null) {
            service.getServices().enqueue(createCallback(services));
        }
        return services;
    }
}
