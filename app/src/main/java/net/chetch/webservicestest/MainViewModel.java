package net.chetch.webservicestest;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.util.Log;

import net.chetch.webservices.DataCache;
import net.chetch.webservices.WebserviceViewModel;
import net.chetch.webservices.employees.Employee;
import net.chetch.webservices.employees.Employees;
import net.chetch.webservices.employees.EmployeesRepository;
import net.chetch.webservices.gps.GPSRepository;
import net.chetch.webservices.network.NetworkRepository;
import net.chetch.webservices.network.Services;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends WebserviceViewModel {

    //repos (make sure you add them so they can be server configured
    GPSRepository gpsRepository = GPSRepository.getInstance();
    EmployeesRepository employeesRepository = EmployeesRepository.getInstance();
    CrewRepository crewRepository = CrewRepository.getInstance().getInstance();

    MutableLiveData<Employees> liveDataEmployees = new MutableLiveData<>();
    MutableLiveData<Crew> liveDataCrew = new MutableLiveData<>();

    public MainViewModel(){
        super();

        //addRepo(employeesRepository);
        addRepo(crewRepository);
        //addRepo("GPS", gpsRepository);
    }

    @Override
    public void loadData(Observer observer){

        super.loadData(data->{
            /*employeesRepository.getEmployees().observe(employees ->{
                employeesRepository.getProfilePics(employees).observe(bms->{

                    employeesRepository.getCacheEntry("employees").forceExpire();
                    employeesRepository.getEmployees();
                    Log.i("Main", "loadData: profile pics loaded");
                });

                Log.i("Main", "loadData: employees loaded");
            });*/


            crewRepository.getCrew().add(liveDataCrew).observe(crew->{
                crewRepository.getProfilePics(crew).observe(bms -> {


                    crewRepository.getCacheEntry("crew").forceExpire();
                    crewRepository.getCrew().observe(crewAgain->{
                       notifyObserver(observer, crewAgain);
                    });

                    Log.i("Main", "loadData: crew profile pics loaded");
                });

                Log.i("Main", "loadData: crew loaded");
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
