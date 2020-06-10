package net.chetch.webservicestest;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.chetch.utilities.Utils;
import net.chetch.webservices.LiveDataCache;
import net.chetch.webservices.Webservice;
import net.chetch.webservices.WebserviceRepository;
import net.chetch.webservices.employees.Employee;
import net.chetch.webservices.employees.EmployeesRepository;
import net.chetch.webservices.employees.IEmployeesService;
import net.chetch.webservices.gps.GPSRepository;
import net.chetch.webservices.network.NetworkRepository;
import net.chetch.utilities.Utils;

public class MainActivity extends AppCompatActivity {


    NetworkRepository networkRepository = new NetworkRepository();
    GPSRepository gpsRepository = new GPSRepository();
    EmployeesRepository employeesRepository = new EmployeesRepository(LiveDataCache.VERY_SHORT_CACHE);

    protected LiveData<String> liveDataTest(){
        MutableLiveData<String> ldt = new MutableLiveData<>();
        ldt.setValue("Hey");
        return ldt;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        employeesRepository.getError().observe(this, t ->{
            Log.e("Main", t.getMessage());
        });

        //build repository
        try {

            String apiBaseURL = "http://192.168.43.123:8002/api/";
            networkRepository.setAPIBaseURL(apiBaseURL);

            apiBaseURL = "http://192.168.43.123:8003/api/";
            gpsRepository.setAPIBaseURL(apiBaseURL);

            apiBaseURL = "http://192.168.43.123:8004/api/";
            employeesRepository.setAPIBaseURL(apiBaseURL);

            employeesRepository.getActiveEmployees().observe(this, emps->{
                ((TextView)findViewById(R.id.tv1)).setText("ER1: " + emps.size() + " " + System.currentTimeMillis());
                Log.i("Main","Employees received 1: " + emps.size());
            });

            employeesRepository.getActiveEmployees().observe(this, emps->{
                ((TextView)findViewById(R.id.tv2)).setText("ER2: " + emps.size() + " " + System.currentTimeMillis());
                Log.i("Main","Employees received 2: " + emps.size());
            });

            Button btn = findViewById(R.id.button);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    employeesRepository.getActiveEmployees();
                }
            });

        } catch (Exception e){
            Log.e("Main", e.getMessage());
        }
    }
}
