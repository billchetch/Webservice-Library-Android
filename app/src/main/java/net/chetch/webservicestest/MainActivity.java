package net.chetch.webservicestest;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.chetch.utilities.Utils;
import net.chetch.webservices.DataObjectCollection;
import net.chetch.webservices.LiveDataCache;
import net.chetch.webservices.Webservice;
import net.chetch.webservices.WebserviceRepository;
import net.chetch.webservices.employees.Employee;
import net.chetch.webservices.employees.Employees;
import net.chetch.webservices.employees.EmployeesRepository;
import net.chetch.webservices.employees.IEmployeesService;
import net.chetch.webservices.gps.GPSRepository;
import net.chetch.webservices.network.NetworkRepository;
import net.chetch.utilities.Utils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    NetworkRepository networkRepository = new NetworkRepository();
    GPSRepository gpsRepository = new GPSRepository();
    EmployeesRepository employeesRepository = new EmployeesRepository(LiveDataCache.VERY_SHORT_CACHE);
    int employeeID;

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

            employeesRepository.getEmployees().observe(this, emps->{
                ((TextView)findViewById(R.id.tv1)).setText("ER1: " + emps.size() + " " + System.currentTimeMillis());

                Employees.SortOn s = emps.createSortOn();
                s.put("last_name", DataObjectCollection.SortOptions.ASC);
                s.put("first_name", DataObjectCollection.SortOptions.DESC);

                emps.sort(s).limit(5);

                Log.i("Main", "Finished");
            });

            /*employeesRepository.g().observe(this, emps->{
                ((TextView)findViewById(R.id.tv2)).setText("ER2: " + emps.size() + " " + System.currentTimeMillis());
                Log.i("Main","Employees received 2: " + emps.size());
            });*/

            Button btn = findViewById(R.id.button);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Employee emp = new Employee();
                    emp.setFirstName("Croll");;
                    emp.setPositionID(1);
                    emp.setLastName("Crallxx" + Math.random());
                    emp.setEmployeeID("88300" + Math.random());
                    employeesRepository.addEmployee(emp).observe(MainActivity.this, employee->{
                        ((TextView)findViewById(R.id.tv2)).setText(employee.getEmployeeID());
                        employeeID = employee.getID();
                        employeesRepository.getEmployees();
                    });

                }
            });

            btn = findViewById(R.id.button2);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    employeesRepository.removeEmployee(employeeID).observe(MainActivity.this, eid->{
                        Log.i("Main", "Delete employees");
                        employeesRepository.getEmployees();
                    });

                }
            });

        } catch (Exception e){
            Log.e("Main", e.getMessage());
        }
    }
}
