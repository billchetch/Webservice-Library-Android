package net.chetch.webservices.network;



import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface INetworkService{
    String SERVICE_NAME = "Network";

    @GET("services")
    Call<Services> getServices();

    @GET("token")
    Call<ServiceToken> getToken(@Query("service_id") int serviceID, @Query("client_name") String clientName);

    @PUT("token")
    Call<ServiceToken> putToken(@Body ServiceToken token);

}
