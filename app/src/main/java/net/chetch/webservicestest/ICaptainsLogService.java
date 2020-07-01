package net.chetch.webservicestest;

import net.chetch.webservices.AboutService;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ICaptainsLogService {
    String SERVICE_NAME = "Captains Log";

    @GET("about")
    Call<AboutService> getAbout();

    @GET("crew-stats")
    Call<CrewStats> getCrewStats();
}
