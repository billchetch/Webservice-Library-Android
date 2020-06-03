package net.chetch.webservices.gps;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import net.chetch.webservices.Webservice;
import net.chetch.webservices.WebserviceRepository;
import java.util.Calendar;

public class GPSRepository extends WebserviceRepository<IGPSService>{

    public GPSRepository() {
        super(new Webservice(IGPSService.class));
    }

    public LiveData<GPSPosition> getLatestPosition(){
        final MutableLiveData<GPSPosition> position = new MutableLiveData<>();

        if(service != null) {
            service.getLatestPosition().enqueue(createCallback(position));
        }
        return position;
    }
}
