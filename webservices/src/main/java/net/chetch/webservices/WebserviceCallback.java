package net.chetch.webservices;

import androidx.lifecycle.Observer;

import net.chetch.webservices.exceptions.WebserviceException;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import okhttp3.Headers;

public class WebserviceCallback<T> implements Callback<T> {

    static public class WebserviceCallbackInfo{
        public boolean responseIsSuccessful = false;
        public Headers headers = null;
        public long responseTime = 0;
    }

    private Throwable lastError;
    private Observer observer;
    private DataStore dataStore = null;


    public WebserviceCallback(Observer observer, DataStore dataStore){
        this.observer = observer;
        this.dataStore = dataStore;
    }

    public Throwable getLastError(){
        return lastError;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        WebserviceCallbackInfo callbackInfo = new WebserviceCallbackInfo();
        callbackInfo.headers = response.headers();
        callbackInfo.responseIsSuccessful = response.isSuccessful();
        callbackInfo.responseTime = response.raw().receivedResponseAtMillis() - response.raw().sentRequestAtMillis();
        if(observer != null){
           observer.onChanged(callbackInfo);
        }

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
        lastError = t;
        if(observer != null){
            WebserviceException wsex = new WebserviceException(t);
            wsex.setDataStore(dataStore);
            observer.onChanged(wsex);
        }
    }

    public void handleResponse(Call<T> call, Response<T> response){
        if(observer != null){
            observer.onChanged(response);
        }

        if(dataStore != null){
            dataStore.updateData(response.body());
        }
    }

    public void handleEmptyResponse(Call<T> call, Response<T> response){
        if(observer != null){
            WebserviceException wsex = new WebserviceException("Empty response body", 0);
            wsex.setDataStore(dataStore);
            observer.onChanged(wsex);
        }
    }

    public void handleError(Call<T> call, Response<T> response){
        if(observer != null){
            WebserviceException wsex = WebserviceException.create(response);
            wsex.setDataStore(dataStore);
            observer.onChanged(wsex);
        }
    }
}
