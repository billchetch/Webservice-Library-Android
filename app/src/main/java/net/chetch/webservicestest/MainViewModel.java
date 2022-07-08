package net.chetch.webservicestest;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.annotation.Nullable;
import android.util.Log;

import net.chetch.webservices.DataStore;
import net.chetch.webservices.WebserviceViewModel;
import net.chetch.webservices.employees.Employee;
import net.chetch.webservices.employees.Employees;
import net.chetch.webservices.employees.EmployeesRepository;
import net.chetch.webservices.gps.GPSPosition;
import net.chetch.webservices.gps.GPSRepository;

public class MainViewModel extends WebserviceViewModel {

    //repos (make sure you add them so they can be server configured
    GPSRepository gpsRepository = GPSRepository.getInstance();
    EmployeesRepository employeesRepository = EmployeesRepository.getInstance();
    CrewRepository crewRepository; // = CrewRepository.getInstance().getInstance();
    CaptainsLogRepository logRepository; // = CaptainsLogRepository.getInstance();

    MutableLiveData<Employees<Employee>> liveDataEmployees = new MutableLiveData<>();
    MutableLiveData<Crew> liveDataCrew = new MutableLiveData<>();
    MutableLiveData<GPSPosition> liveDataGPSPosition = new MutableLiveData<>();

    public MainViewModel(){
        super();

        permissableServerTimeDifference = 1;
        serverTimeDisparityOption = ServerTimeDisparityOptions.LOG_WARNING;

        //addRepo(employeesRepository);
        //addRepo(crewRepository);
        //addRepo(logRepository);
        addRepo(gpsRepository);
    }


    @Override
    public DataStore<?> loadData(Observer observer){
        try {
            DataStore<?> dataStore = super.loadData(observer);
            return dataStore;
        } catch (Exception e){
            Log.e("MVM", e.getMessage());
            return null;
        }
    }

    public LiveData<Employees<Employee>> getEmployees(){
        employeesRepository.getEmployees().add(liveDataEmployees);
        return liveDataEmployees;
    }

    public void refreshEmployees(){
        employeesRepository.getEmployees().notifyObservers();
    }

    public void addEmployee(Employee emp){
        crewRepository.addEmployee(emp).observe(newEmp->{
            Log.i("MVM", "New employee added");
        });
    }

    public LiveData<GPSPosition> getGPSPosition(){
        return liveDataGPSPosition;
    }

    public LiveData<GPSPosition> getLatestGPSPosition(){
        gpsRepository.getLatestPosition().add(liveDataGPSPosition);
        return liveDataGPSPosition;
    }
}
