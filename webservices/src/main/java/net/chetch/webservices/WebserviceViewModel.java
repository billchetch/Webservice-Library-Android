package net.chetch.webservices;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import android.util.Log;

import net.chetch.webservices.exceptions.WebserviceViewModelException;
import net.chetch.webservices.network.NetworkRepository;
import net.chetch.webservices.network.Services;
import net.chetch.webservices.network.Service;

import java.util.HashMap;
import java.util.Map;

public class WebserviceViewModel extends ViewModel {

    protected enum ServerTimeDisparityOptions{
        ERROR,
        ADUST,
        LOG_WARNING
    }

    public class LoadProgress{
        public String info;
        public Object dataLoaded;
        public int loadedCount = -1;
        public boolean startedLoading = false;
        public boolean finishedLoading = false;

        public void reset(){
            info = null;
            loadedCount = 0;
            startedLoading = false;
            finishedLoading = false;
            dataLoaded = null;
        }

        public void start(){
            startedLoading = true;
            finishedLoading = false;
        }

        public void stop(Object dataLoaded){
            startedLoading = false;
            finishedLoading = true;
            this.dataLoaded = dataLoaded;
            loadedCount++;
        }
    }

    protected NetworkRepository networkRepository = NetworkRepository.getInstance();
    MutableLiveData<Throwable> error = new MutableLiveData<>();
    HashMap<String, WebserviceRepository> repos = new HashMap<>();

    protected LoadProgress loadProgress = new LoadProgress();
    protected boolean servicesConfigured = false;
    protected int permissableServerTimeDifference = 60; //in seconds
    protected ServerTimeDisparityOptions serverTimeDisparityOption = ServerTimeDisparityOptions.ERROR;

    public WebserviceViewModel() {
        observeError(networkRepository);
    }

    protected void observeError(WebserviceRepository<?> repo){
        repo.getError().observeForever(t ->{
            error.setValue(t);
            Log.e("WSVM", "Network repo" + t.getMessage());
        });
    }

    public void setNetworkAPIURL(String apiBaseURL){
        try {
            //String apiBaseURL = "http://192.168.43.123:8002/api/";
            networkRepository.setAPIBaseURL(apiBaseURL);
        } catch (Exception e) {
            Log.e("WSVM", e.getMessage());
            error.setValue(e);
            return;
        }
    }

    public LiveData<Throwable> getError() {
        return error;
    }

    protected void setError(Throwable t){
        error.postValue(t);
    }

    protected void setError(String errmsg){
        WebserviceViewModelException e = new WebserviceViewModelException(errmsg);
        setError(e);
    }

    public WebserviceRepository addRepo(String serviceName, WebserviceRepository<?> repo) throws Exception {
        if(repos.containsKey(serviceName)){
            throw new Exception("Cannot add repo with service " + serviceName + " as a repo already exists for that service");
        }
        repos.put(serviceName, repo);
        observeError(repo);
        servicesConfigured = false;
        return repo;
    }

    public WebserviceRepository addRepo(WebserviceRepository<?> repo){
        try {
            return addRepo(repo.webservice.getDefaultName(), repo);
        } catch(Exception e){
            error.setValue(e);
            return null;
        }
    }

    public WebserviceRepository getRepo(String serviceName) {
        return repos.containsKey(serviceName) ? repos.get(serviceName) : null;
    }

    public void loadData(){
        loadData(null);
    }

    public DataStore loadData(Observer observer){
        loadProgress.reset();
        return loadServices(observer);
    }

    protected void notifyObserver(Observer observer, Object data){
        if(observer != null){
            try {
                observer.onChanged(data);
            } catch (Exception e){
                Log.e("WSVM", e.getMessage());
            }
        }
    }

    protected void notifyLoading(Observer observer, String info){
        notifyLoading(observer, info, null);
    }

    protected void notifyLoading(Observer observer, String info, Object dataPreviouslyLoaded){
        if(dataPreviouslyLoaded != null){
            notifyLoaded(observer, dataPreviouslyLoaded);
        }

        loadProgress.start();
        loadProgress.info = info;
        notifyObserver(observer, loadProgress);
    }

    protected void notifyLoaded(Observer observer, Object data){
        loadProgress.stop(data);
        notifyObserver(observer, loadProgress);
    }

    protected ServerTimeDisparityOptions getServerTimeDisparityOption(long serverTimeDifference){
        return serverTimeDisparityOption;
    }

    protected void configureRepoService(WebserviceRepository<?> repo, Service service) throws Exception{
        repo.setAPIBaseURL(service.getLocalEndpoint());
        repo.synchronise(networkRepository);
    }

    protected boolean configureServices(Services services) {
        if(!networkRepository.isSynchronisedWithServer(permissableServerTimeDifference)){
            long diff = networkRepository.getServerTimeDifference();
            ServerTimeDisparityOptions option = getServerTimeDisparityOption(diff);
            String message = "Server is not synchronised.  Time difference is " + diff + "ms, permissable difference is " + permissableServerTimeDifference*1000 + "ms";
            switch(option){
                case ERROR:
                    error.setValue(new WebserviceViewModelException(message));
                    Log.e("WSVM", message);
                    return false;
                case ADUST:
                    networkRepository.adjustForServerTimeDifference(true);
                    Log.i("WSVM", message + "... Setting repos to adjust for time difference");
                    break;
                case LOG_WARNING:
                    Log.w("WSVM", message);
                    break;
            }
        }

        boolean configured = true;
        for(Map.Entry<String, WebserviceRepository> entry : repos.entrySet()) {
            try {
                String serviceName = entry.getKey();
                WebserviceRepository repo = entry.getValue();
                if (services.hasService(serviceName)) {
                    configureRepoService(repo, services.getService(serviceName));
                } else {
                    throw new Exception("There is no service with name " + serviceName);
                }
            } catch (Exception e) {
                Log.e("WSVM", e.getMessage());
                error.setValue(e);
                configured = false;
            }
        }
        return configured;
    }

    protected DataStore<Services> loadServices(Observer observer){
        return networkRepository.getServices().observe(services -> {
            servicesConfigured = configureServices(services);
            if (servicesConfigured) {
                notifyLoaded(observer, services);
                Log.i("WSVM", "Network services: " + services.size());
            } else {
                Log.e("WSVM", "Services failed to be configured");
            }
        });
    }

    public boolean isServicesConfigured(){
        return servicesConfigured;
    }
}