package net.chetch.webservices.employees;



import net.chetch.webservices.DataObjectCollection;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IEmployeesService{
    @GET("employees")
    Call<Employees> getEmployees(@Query("position_id") int positionID);

    @GET("active-employees")
    Call<Employees> getActiveEmployees(@Query("position_id") int positionID);

    @PUT("employee/{eid}")
    Call<Employee> putEmployee(@Body Employee employee, @Path("eid") int employeeID);

    @DELETE("employee/{eid}")
    Call<Integer> deleteEmployee(@Path("eid") int employeeID);
}
