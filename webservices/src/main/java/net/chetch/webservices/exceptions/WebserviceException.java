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
        WebserviceException wsex = null;
        String errorBody = null;
        try {
            errorBody = response.errorBody().string();
            Gson gson = new GsonBuilder().create();
            ErrorResponse errorResponse = gson.fromJson(errorBody, ErrorResponse.class);
            wsex = new WebserviceException(errorResponse.message, errorResponse.errorCode, response.code());
        } catch (Exception e) {
            errorBody = "WebserviceException.create : " + e.getMessage();
            wsex = new WebserviceException(errorBody, 0, response.code());
        }
        return wsex;
    }

    public static WebserviceException create(Response<?> response, int errorCode){
        WebserviceException wsex = create(response);
        wsex.setErrorCode(errorCode);
        return wsex;
    }

    private Throwable throwable;
    private int errorCode;
    private int httpCode;
    private boolean serviceAvailable = true;

    public WebserviceException(String message, int errorCode, int httpCode){
        super(message);
        this.errorCode = errorCode;
        this.httpCode = httpCode;
    }

    public WebserviceException(String message, int errorCode, Throwable throwable){
        super(message);
        this.throwable = throwable;
        this.errorCode = errorCode;
    }

    public WebserviceException(String message, int errorCode){
        super(message);
        this.errorCode = errorCode;
    }

    public void setServiceAvailable(boolean available){
        serviceAvailable = available;
    }

    public boolean isServiceAvailable(){
        return serviceAvailable;
    }
    public void setHttpCode(int hc){ this.httpCode = hc; }
    public int getHttpCode(){ return httpCode; }
    public void setErrorCode(int errorCode){ this.errorCode = errorCode; }
    public int getErrorCode(){ return errorCode; }
}
