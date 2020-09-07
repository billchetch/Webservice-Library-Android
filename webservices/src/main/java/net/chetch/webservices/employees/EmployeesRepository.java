package net.chetch.webservices.employees;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.graphics.Bitmap;

import net.chetch.utilities.Utils;
import net.chetch.webservices.AboutService;
import net.chetch.webservices.DataCache;
import net.chetch.webservices.DataStore;
import net.chetch.webservices.WebserviceRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

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


    @Override
    protected void handleResponse(Response response) {
        super.handleResponse(response);

        if(response.body() instanceof Employees){
            for(Object obj : (Employees)response.body()){
                Employee emp = (Employee)obj;
                String key = "profile-pic-" + emp.getEmployeeID();
                if(!cache.entryIsEmpty(key)){
                    emp.profileImage = cache.<Bitmap>getCacheEntry(key).getData();
                }
            }
        }
    }

    public DataStore<AboutService> getAbout(){
        //DataCache.CacheEntry<AboutService> entry = cache.getCacheEntry("about-service");

        DataStore<AboutService> entry = new DataStore<>(); //cache.getCacheEntry("about-service");
        //if(entry.requiresUpdating()) {
            service.getAbout().enqueue(createCallback(entry));
        //}

        return entry;
    }

    public DataStore<Employee> addEmployee(Employee employee){
        final DataStore<Employee> dsEmployee = new DataStore<>();

        service.putEmployee(employee, employee.getID()).enqueue(createCallback(dsEmployee));

        cache.forceExpire("employees", "active-employees");

        return dsEmployee;
    }

    public DataStore<Integer> removeEmployee(int id){
        final DataStore<Integer> dsID = new DataStore<>();

        service.deleteEmployee(id).enqueue(createCallback(dsID));

        cache.forceExpire("employees", "active-employees");

        return dsID;
    }

    public DataStore<Employees<Employee>> getActiveEmployees(){
        DataCache.CacheEntry<Employees<Employee>> entry = cache.getCacheEntry("active-employees");

        if(entry.requiresUpdating()) {
            service.getActiveEmployees(0).enqueue(createCallback(entry));
        }

        return entry;
    }

    public DataStore<Employees<Employee>> getEmployees(){
        DataCache.CacheEntry<Employees<Employee>> entry = cache.getCacheEntry("employees");

        if(entry.requiresUpdating()) {
            service.getEmployees(0).enqueue(createCallback(entry));
        }

        return entry;
    }

    public DataStore<HashMap<String, Bitmap>> getProfilePics(List<Employee> employees){
        DataStore<HashMap<String, Bitmap>> ds = new DataStore<>();

        HashMap<String, Employee> url2emps = new HashMap<>();
        for(Employee emp : employees){
            String src = webservice.getAPIBaseURL() + "/resource/image/profile-pics/" + emp.getEmployeeID();
            url2emps.put(src, emp);
        }

        Utils.downloadImages(url2emps.keySet(), bms->{
            for(Map.Entry<String, Bitmap> entry : bms.entrySet()){
                Employee emp = url2emps.get(entry.getKey());
                emp.profileImage = entry.getValue();

                DataCache.CacheEntry<Bitmap> ce = cache.getCacheEntry("profile-pic-" + emp.getEmployeeID());
                ce.updateData(emp.profileImage);
            }

            ds.updateData(bms);
        });

        return ds;
    }

}
