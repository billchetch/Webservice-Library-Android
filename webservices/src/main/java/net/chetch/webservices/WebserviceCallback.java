package net.chetch.webservices;

import android.arch.lifecycle.MutableLiveData;

import net.chetch.webservices.exceptions.WebserviceException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebserviceCallback<T> implements Callback<T> {

    private MutableLiveData<Throwable> liveDataError;
    private MutableLiveData<T> liveDataResponse;

    public WebserviceCallback(MutableLiveData<Throwable> liveDataError, MutableLiveData<T> liveDataResponse){
        this.liveDataError = liveDataError;
        this.liveDataResponse = liveDataResponse;
    }

    public Throwable getLastError(){
        return liveDataError.getValue();
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if(response.isSuccessful()){
            if(response.body() == null){
                handleEmptyResponse(call, response);
            } else {
                handleResponse(call, response);
            }
        } else {
            handleError(call, response);
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        if(liveDataError != null && t != null){
            liveDataError.setValue(t);
        }
    }

    public void handleResponse(Call<T> call, Response<T> response){
        if(liveDataResponse != null){
            liveDataResponse.setValue(response.body());
        }
    }

    public void handleEmptyResponse(Call<T> call, Response<T> response){
        if(liveDataError != null) {
            liveDataError.setValue(new Exception("Empty response body"));
        }
    }

    public void handleError(Call<T> call, Response<T> response){
        if(liveDataError != null) {
            WebserviceException sfex = WebserviceException.create(response);
            liveDataError.setValue(sfex);
        }
    }

}
