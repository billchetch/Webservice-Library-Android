package net.chetch.webservicestest;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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
            Log.e("Main", "Error: " + t.getMessage());
        });

        model.getEmployees().observe(this, employees->{
            log("Has " + employees.size() + " employees " + Calendar.getInstance().getTimeInMillis());
        });

        model.getEmployees().observe(this, employees->{
            log("YOOOOO" + employees.size() + " employees " + Calendar.getInstance().getTimeInMillis());
        });

        model.loadData();

        //build repository
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
                    model.loadData();
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
