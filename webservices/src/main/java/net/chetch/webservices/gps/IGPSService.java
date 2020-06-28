package net.chetch.webservices.gps;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IGPSService {
    String SERVICE_NAME = "GPS";

    @GET("latest-position")
    Call<GPSPosition> getLatestPosition();
}
