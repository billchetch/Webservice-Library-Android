package net.chetch.webservices.employees;



import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IEmployeesService{
    @GET("employees")
    Call<List<Employee>> getEmployees(@Query("position_id") int positionID);

    @GET("active-employees")
    Call<List<Employee>> getActiveEmployees(@Query("position_id") int positionID);

    @PUT("employee/{eid}")
    Call<Employee> putEmployee(@Body Employee employee, @Path("eid") int employeeID);
}
