package net.chetch.webservices;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;

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
    private MutableLiveData<T> liveDataResponse = null;
    private DataCache.CacheEntry cacheEntry = null;

    public WebserviceCallback(Observer observer, MutableLiveData<T> liveDataResponse){
        this.observer = observer;
        this.liveDataResponse = liveDataResponse;
    }

    public WebserviceCallback(Observer observer, DataCache.CacheEntry cacheEntry){
        this.observer = observer;
        this.cacheEntry = cacheEntry;
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
            observer.onChanged(t);
        }
    }

    public void handleResponse(Call<T> call, Response<T> response){
        if(cacheEntry != null){
            cacheEntry.updateValue(response.body());
        }
        if(liveDataResponse != null){
            liveDataResponse.setValue(response.body());
        }
        if(observer != null){
            observer.onChanged(response);
        }
    }

    public void handleEmptyResponse(Call<T> call, Response<T> response){
        if(observer != null){
            observer.onChanged(new Exception("Empty response body"));
        }
    }

    public void handleError(Call<T> call, Response<T> response){
        WebserviceException wsex = WebserviceException.create(response);
        if(observer != null){
            observer.onChanged(wsex);
        }
    }

}
