package net.chetch.webservicestest;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import net.chetch.webservices.DataObject;
import net.chetch.webservices.DataStore;
import net.chetch.webservices.Webservice;
import net.chetch.webservices.employees.Employee;
import net.chetch.webservices.employees.Employees;
import net.chetch.webservices.network.NetworkRepository;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    MainViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            String apiBaseURL = "http://192.168.43.123:8002/api/";
            NetworkRepository.getInstance().setAPIBaseURL(apiBaseURL);
        } catch (Exception e) {
            Log.e("MVM", e.getMessage());
            return;
        }

        model = ViewModelProviders.of(this).get(MainViewModel.class);
        model.getError().observe(this, t ->{
            Log.e("Main", "MODEL ERROR!!!!!!!!!!!: " + t.getMessage());
        });


        model.loadData(data->{
            Log.i("Main", "Loaded data");
        });

        /*model.getEmployees().observe(this, employees->{
            log("Has " + employees.size() + " employees " + Calendar.getInstance().getTimeInMillis());
        });

        model.getEmployees().observe(this, employees->{
            log("YOOOOO" + employees.size() + " employees " + Calendar.getInstance().getTimeInMillis());
        });*/



        try {

            Button btn = findViewById(R.id.button);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    model.refreshEmployees();
                }
            });

            btn = findViewById(R.id.button2);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Employee emp = new Employee();
                    emp.setValue("employee_id", "xxxx");
                    emp.setValue("last_name", "Cron");
                    emp.setValue("first_name", "Test");
                    emp.setValue("position_id", 1);

                    model.addEmployee(emp);
                }
            });

        } catch (Exception e){
            Log.e("Main", e.getMessage());
        }
    }



    protected void log(String s){
        Log.i("Main", s);
    }


}
