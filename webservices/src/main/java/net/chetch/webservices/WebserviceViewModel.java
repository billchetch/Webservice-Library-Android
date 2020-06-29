package net.chetch.webservices;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import net.chetch.webservices.employees.Employees;
import net.chetch.webservices.employees.EmployeesRepository;
import net.chetch.webservices.gps.GPSRepository;
import net.chetch.webservices.network.NetworkRepository;
import net.chetch.webservices.network.Services;
import net.chetch.webservices.network.Service;

import java.util.HashMap;
import java.util.Map;

public class WebserviceViewModel extends ViewModel {

    protected NetworkRepository networkRepository = NetworkRepository.getInstance();
    MutableLiveData<Throwable> error = new MutableLiveData<>();
    HashMap<String, WebserviceRepository> repos = new HashMap<>();

    protected boolean servicesConfigured = false;

    public WebserviceViewModel() {
        observeError(networkRepository);
    }

    protected void observeError(WebserviceRepository<?> repo){
        repo.getError().observeForever(t ->{
            error.setValue(t);
            Log.e("MVM", "Network repo" + t.getMessage());
        });
    }

    public void setNetworkAPIURL(String apiBaseURL){
        try {
            //String apiBaseURL = "http://192.168.43.123:8002/api/";
            networkRepository.setAPIBaseURL(apiBaseURL);
        } catch (Exception e) {
            Log.e("MVM", e.getMessage());
            error.setValue(e);
            return;
        }
    }

    public LiveData<Throwable> getError() {
        return error;
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

    public void loadData(Observer observer){
        loadServices(observer);
    }

    protected void notifyObserver(Observer observer, Object data){
        if(observer != null){
            try {
                observer.onChanged(data);
            } catch (Exception e){
                Log.e("MVM", e.getMessage());
            }
        }
    }

    protected void configureRepoService(WebserviceRepository<?> repo, Service service) throws Exception{
        repo.setAPIBaseURL(service.getLocalEndpoint());
        repo.synchronise(networkRepository);
    }

    protected void configureServices(Services services) {
        if(!networkRepository.isSynchronisedWithServer(0)){
            networkRepository.adjustForServerTimeDifference(true);
        }

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
                Log.e("MVM", e.getMessage());
                error.setValue(e);
            }
        }
    }

    protected DataStore loadServices(Observer observer){
        return networkRepository.getServices().observe(services -> {
            configureServices(services);
            servicesConfigured = true;
            notifyObserver(observer, services);
            Log.i("MVM", "Network services: " + services.size());
        });
    }


}