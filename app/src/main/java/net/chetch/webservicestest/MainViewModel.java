package net.chetch.webservicestest;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.util.Log;

import net.chetch.webservices.DataCache;
import net.chetch.webservices.WebserviceViewModel;
import net.chetch.webservices.employees.Employees;
import net.chetch.webservices.employees.EmployeesRepository;
import net.chetch.webservices.gps.GPSRepository;
import net.chetch.webservices.network.NetworkRepository;
import net.chetch.webservices.network.Services;

public class MainViewModel extends WebserviceViewModel {

    //repos (make sure you add them so they can be server configured
    GPSRepository gpsRepository = GPSRepository.getInstance();
    EmployeesRepository employeesRepository = EmployeesRepository.getInstance();

    MutableLiveData<Employees> liveDataEmployees = new MutableLiveData<>();

    public MainViewModel(){
        super();

        addRepo("Employees", employeesRepository);
        //addRepo("GPS", gpsRepository);
    }

    @Override
    public void loadData(Observer observer){
        super.loadData(data->{
            employeesRepository.getEmployees().add(liveDataEmployees).observe(employees->{
                //employees loaded
                notifyObserver(observer, employees);
            });
        });
    }

    public LiveData<Employees> getEmployees(){
        return liveDataEmployees;
    }

    public void refreshEmployees(){
        employeesRepository.getEmployees().notifyObservers();
    }
}
