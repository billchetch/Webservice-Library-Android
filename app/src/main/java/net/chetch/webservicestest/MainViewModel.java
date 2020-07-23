package net.chetch.webservicestest;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;

import net.chetch.webservices.DataCache;
import net.chetch.webservices.DataObjectCollection;
import net.chetch.webservices.DataStore;
import net.chetch.webservices.WebserviceViewModel;
import net.chetch.webservices.employees.Employee;
import net.chetch.webservices.employees.Employees;
import net.chetch.webservices.employees.EmployeesRepository;
import net.chetch.webservices.gps.GPSPosition;
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
    CaptainsLogRepository logRepository = CaptainsLogRepository.getInstance();

    MutableLiveData<Employees> liveDataEmployees = new MutableLiveData<>();
    MutableLiveData<Crew> liveDataCrew = new MutableLiveData<>();
    MutableLiveData<GPSPosition> liveDataGPSPosition = new MutableLiveData<>();

    public MainViewModel(){
        super();

        permissableServerTimeDifference = 1;
        serverTimeDisparityOption = ServerTimeDisparityOptions.LOG_WARNING;

        addRepo(employeesRepository);
        //addRepo(crewRepository);
        addRepo(logRepository);
        addRepo(gpsRepository);
    }


    @Override
    public DataStore<?> loadData(Observer observer){
        DataStore<?> dataStore = super.loadData(observer);
        /*dataStore.observe(data->{

            notifyLoading(observer, "Employees");
            employeesRepository.getEmployees().observe(employees ->{
                notifyLoading(observer, "GPS", employees);
                gpsRepository.getLatestPosition().observe(gps ->{
                    notifyLoaded(observer, gps);
                });
            });

            /*crewRepository.getAbout().observe(about->{
               Log.i("Main", "About ya");
            });*/

            /*logRepository.getCrewStats().observe(stats->{
                Log.i("MVM", "Crew stats");
            });*/

            /*crewRepository.getCrew().add(liveDataCrew).observe(crew->{

                crew.sort("known_as", DataObjectCollection.SortOptions.DESC);
                Crew f = crew.filter("known_as","Test");
                CrewMember m = crew.get("known_as", "Test");

                crewRepository.getProfilePics(crew).observe(bms -> {


                    crewRepository.getCacheEntry("crew").forceExpire();
                    crewRepository.getCrew().observe(crewAgain->{
                       notifyObserver(observer, crewAgain);
                    });

                    Log.i("Main", "loadData: crew profile pics loaded");
                });

                Log.i("Main", "loadData: crew loaded");
            });
        });*/

        return dataStore;
    }

    public LiveData<Employees> getEmployees(){
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
