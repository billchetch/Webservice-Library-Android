package net.chetch.webservicestest;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import net.chetch.webservices.WebserviceViewModel;
import net.chetch.webservices.network.NetworkRepository;


public class MainActivity extends AppCompatActivity {

    MainViewModel model;
    Observer dataLoadObserver = obj -> {
        WebserviceViewModel.LoadProgress progress = (WebserviceViewModel.LoadProgress)obj;
        String state = progress.startedLoading ? "loading" : "loaded";
        Log.i("Main", "load observer " + state + " " + progress.info);
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            //String apiBaseURL = "http://192.168.43.123:8001/api/";
            String apiBaseURL = "http://192.168.1.100:8001/api/";
            NetworkRepository.getInstance().setAPIBaseURL(apiBaseURL);
        } catch (Exception e) {
            Log.e("MVM", e.getMessage());
            return;
        }

        model = ViewModelProviders.of(this).get(MainViewModel.class);
        model.getError().observe(this, t ->{
            Log.e("Main", "MODEL ERROR!!!!!!!!!!!: " + t.getMessage());
        });


        model.loadData(dataLoadObserver).observe(data->{
            Log.i("Main", "loaded data");
            //model.getLatestGPSPosition();
        });

        try {

            Button btn = findViewById(R.id.button);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    model.getLatestGPSPosition();

                }
            });

            btn = findViewById(R.id.button2);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

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
