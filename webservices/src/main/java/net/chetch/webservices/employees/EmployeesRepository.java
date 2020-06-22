package net.chetch.webservices.employees;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import net.chetch.webservices.AboutService;
import net.chetch.webservices.DataObjectCollection;
import net.chetch.webservices.LiveDataCache;
import net.chetch.webservices.Webservice;
import net.chetch.webservices.WebserviceCallback;
import net.chetch.webservices.WebserviceRepository;
import net.chetch.webservices.gps.IGPSService;

import java.util.List;

public class EmployeesRepository extends WebserviceRepository<IEmployeesService> {

    static private EmployeesRepository instance = null;
    static public EmployeesRepository getInstance(){
        return instance == null ? new EmployeesRepository() : instance;
    }

    public EmployeesRepository() {
        super(IEmployeesService.class);
    }

    public EmployeesRepository(int defaultCacheTime) {
        super(IEmployeesService.class, defaultCacheTime);
    }

    public LiveData<AboutService> getAbout(){
        LiveDataCache.CacheEntry entry = cache.<Employees>getCacheEntry("about-service");

        if(entry.refreshValue()) {
            service.getAbout().enqueue(createCallback(entry));
        }

        return entry.liveData;
    }

    public LiveData<Employee> addEmployee(Employee employee){
        final MutableLiveData<Employee> liveDataEmployee = new MutableLiveData<>();

        service.putEmployee(employee, employee.getID()).enqueue(createCallback(liveDataEmployee));

        cache.setRefreshOnNextCall("employees");
        cache.setRefreshOnNextCall("active-employees");

        return liveDataEmployee;
    }

    public LiveData<Integer> removeEmployee(int id){
        final MutableLiveData<Integer> liveDataID = new MutableLiveData<>();

        service.deleteEmployee(id).enqueue(createCallback(liveDataID));

        cache.setRefreshOnNextCall("employees");
        cache.setRefreshOnNextCall("active-employees");

        return liveDataID;
    }

    public LiveData<Employees> getActiveEmployees(){
        LiveDataCache.CacheEntry entry = cache.<Employees>getCacheEntry("active-employees");

        if(entry.refreshValue()) {
            service.getActiveEmployees(0).enqueue(createCallback(entry));
        }

        return entry.liveData;
    }

    public LiveData<Employees> getEmployees(){
        LiveDataCache.CacheEntry entry = cache.<Employees>getCacheEntry("employees");

        if(entry.refreshValue()) {
            service.getEmployees(0).enqueue(createCallback(entry));
        }

        return entry.liveData;
    }


}
