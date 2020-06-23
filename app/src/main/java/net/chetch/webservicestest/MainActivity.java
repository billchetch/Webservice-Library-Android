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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

import java.util.Calendar;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {


    NetworkRepository networkRepository = NetworkRepository.getInstance();
    GPSRepository gpsRepository = new GPSRepository();
    EmployeesRepository employeesRepository = EmployeesRepository.getInstance();
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

        networkRepository.getError().observe(this, t ->{
            Log.e("Main", t.getMessage());
        });

        //build repository
        try {

            String apiBaseURL = "http://192.168.43.123:8002/api/";
            networkRepository.setAPIBaseURL(apiBaseURL);

            networkRepository.getServices().observe(this, services->{

                Log.i("Main","Network services: " + services.size());
            });

            Button btn = findViewById(R.id.button);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Employee emp = new Employee();
                    emp.set("first_name", "Croll");;
                    emp.set("position_id", 1);
                    emp.set("last_name", "Crallxx" + Math.random());
                    emp.set("employee_id", "88300" + Math.random());

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
