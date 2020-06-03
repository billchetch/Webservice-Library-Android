package net.chetch.webservicestest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import net.chetch.utilities.Utils;
import net.chetch.webservices.Webservice;
import net.chetch.webservices.gps.GPSRepository;
import net.chetch.webservices.network.NetworkRepository;
import net.chetch.utilities.Utils;

public class MainActivity extends AppCompatActivity {

    NetworkRepository networkRepository = new NetworkRepository();
    GPSRepository gpsRepository = new GPSRepository();

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

            apiBaseURL = "http://192.168.43.123:8003/api/";
            gpsRepository.setAPIBaseURL(apiBaseURL);

            /*networkRepository.getServices().observe(this, services -> {
                Log.i("Main", "Services returned");
            });*/

            gpsRepository.getLatestPosition().observe(this, position ->{
                Log.i("Main", Utils.formatDate(position.getUpdated(), "yyyy-MM-dd HH:mm:ss Z"));
                Log.i("Main", "Latest position returned");
            });

        } catch (Exception e){
            Log.e("Main", e.getMessage());
        }
    }
}
