package net.chetch.webservices.employees;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import net.chetch.webservices.AboutService;
import net.chetch.webservices.DataCache;
import net.chetch.webservices.DataObjectCollection;
import net.chetch.webservices.DataCache;
import net.chetch.webservices.Webservice;
import net.chetch.webservices.WebserviceCallback;
import net.chetch.webservices.WebserviceRepository;
import net.chetch.webservices.gps.IGPSService;

import java.util.List;

public class EmployeesRepository extends WebserviceRepository<IEmployeesService> {

    static private EmployeesRepository instance = null;
    static public EmployeesRepository getInstance(){
        if(instance == null)instance = new EmployeesRepository();
        return instance;
    }

    public EmployeesRepository() {
        super(IEmployeesService.class);
    }

    public EmployeesRepository(int defaultCacheTime) {
        super(IEmployeesService.class, defaultCacheTime);
    }

    public DataCache.CacheEntry getAbout(){
        DataCache.CacheEntry entry = cache.getCacheEntry("about-service");

        if(entry.hasExpired()) {
            service.getAbout().enqueue(createCallback(entry));
        }

        return entry;
    }

    public LiveData<Employee> addEmployee(Employee employee){
        final MutableLiveData<Employee> liveDataEmployee = new MutableLiveData<>();

        service.putEmployee(employee, employee.getID()).enqueue(createCallback(liveDataEmployee));

        cache.setExpireOnNextCall("employees", "active-employees");

        return liveDataEmployee;
    }

    public LiveData<Integer> removeEmployee(int id){
        final MutableLiveData<Integer> liveDataID = new MutableLiveData<>();

        service.deleteEmployee(id).enqueue(createCallback(liveDataID));

        cache.setExpireOnNextCall("employees", "active-employees");

        return liveDataID;
    }

    public DataCache.CacheEntry getActiveEmployees(){
        DataCache.CacheEntry entry = cache.getCacheEntry("active-employees");

        if(entry.hasExpired()) {
            service.getActiveEmployees(0).enqueue(createCallback(entry));
        }

        return entry;
    }

    public DataCache.CacheEntry getEmployees(){
        DataCache.CacheEntry entry = cache.getCacheEntry("employees");

        if(entry.hasExpired()) {
            service.getEmployees(0).enqueue(createCallback(entry));
        }

        return entry;
    }


}
