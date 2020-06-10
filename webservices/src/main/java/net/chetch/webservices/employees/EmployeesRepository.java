package net.chetch.webservices.employees;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import net.chetch.webservices.LiveDataCache;
import net.chetch.webservices.Webservice;
import net.chetch.webservices.WebserviceCallback;
import net.chetch.webservices.WebserviceRepository;
import net.chetch.webservices.gps.IGPSService;

import java.util.List;

public class EmployeesRepository extends WebserviceRepository<IEmployeesService> {

    public EmployeesRepository() {
        super(IEmployeesService.class);
    }

    public EmployeesRepository(int defaultCacheTime) {
        super(IEmployeesService.class, defaultCacheTime);
    }

    public void addEmployee(Employee employee){

        service.putEmployee(employee, employee.getID());

        if(cache.setRefreshOnNextCall("employees"))getEmployees();
        if(cache.setRefreshOnNextCall("active-employees"))getActiveEmployees();
    }

    public LiveData<List<Employee>> getActiveEmployees(){
        LiveDataCache.CacheEntry entry = cache.<List<Employee>>getCacheEntry("active-employees");

        if(entry.refreshValue()) {
            service.getActiveEmployees(0).enqueue(createCallback(entry));
        }

        return entry.liveData;
    }

    public LiveData<List<Employee>> getEmployees(){
        LiveDataCache.CacheEntry entry = cache.<List<Employee>>getCacheEntry("employees");

        if(entry.refreshValue()) {
            service.getEmployees(0).enqueue(createCallback(entry));
        }

        return entry.liveData;
    }
}
