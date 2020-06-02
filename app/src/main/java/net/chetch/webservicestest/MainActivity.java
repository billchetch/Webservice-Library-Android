package net.chetch.webservicestest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import net.chetch.webservices.network.NetworkRepository;


public class MainActivity extends AppCompatActivity {

    NetworkRepository networkRepository = new NetworkRepository();


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

            networkRepository.getServices().observe(this, services -> {
                Log.i("Main", "Services returned");
            });
        } catch (Exception e){
            Log.e("Main", e.getMessage());
        }
    }
}
