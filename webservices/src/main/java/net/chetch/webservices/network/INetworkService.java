package net.chetch.webservices.network;



import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface INetworkService{
    @GET("services")
    Call<HashMap<String, Service>> getServices();
}
