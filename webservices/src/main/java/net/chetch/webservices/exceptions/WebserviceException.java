package net.chetch.webservices.exceptions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import retrofit2.Response;

public class WebserviceException extends Exception {

    public class ErrorResponse{
        @SerializedName("http_code")
        public int httpCode;
        @SerializedName("error_code")
        public int errorCode;
        @SerializedName("message")
        public String message;
    }

    public static WebserviceException create(Response<?> response){
        WebserviceException sfex = null;
        String errorBody = null;
        try {
            errorBody = response.errorBody().string();
            Gson gson = new GsonBuilder().create();
            ErrorResponse errorResponse = gson.fromJson(errorBody, ErrorResponse.class);
            sfex = new WebserviceException(errorResponse.message, errorResponse.errorCode, response.code());
        } catch (Exception e) {
            errorBody = "SurfForecastException.create : " + e.getMessage();
            sfex = new WebserviceException(errorBody, 0, response.code());
        }
        return sfex;
    }

    public static WebserviceException create(String serviceID, Response<?> response, int errorCode){
        WebserviceException sfex = create(response);
        sfex.setErrorCode(errorCode);
        return sfex;
    }

    private String serviceID;
    private int errorCode;
    private int httpCode;

    WebserviceException(String message, int errorCode, int httpCode){
        super(message);
        //this.serviceID = serviceID;
        this.errorCode = errorCode;
        this.httpCode = httpCode;
    }

    public void setHttpCode(int hc){ this.httpCode = hc; }
    public int getHttpCode(){ return httpCode; }
    public void setErrorCode(int errorCode){ this.errorCode = errorCode; }
    public int getErrorCode(){ return errorCode; }
}
