package net.chetch.webservices;

import android.arch.lifecycle.MutableLiveData;

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

    private MutableLiveData<Throwable> liveDataError;
    private MutableLiveData<T> liveDataResponse = null;
    private LiveDataCache.CacheEntry cacheEntry = null;
    private MutableLiveData<WebserviceCallbackInfo> liveDataCallbackInfo = null;

    public WebserviceCallback(MutableLiveData<Throwable> liveDataError, MutableLiveData<T> liveDataResponse, MutableLiveData<WebserviceCallbackInfo> liveDataCallbackInfo){
        this.liveDataError = liveDataError;
        this.liveDataResponse = liveDataResponse;
        this.liveDataCallbackInfo = liveDataCallbackInfo;
    }

    public WebserviceCallback(MutableLiveData<Throwable> liveDataError, LiveDataCache.CacheEntry cacheEntry, MutableLiveData<WebserviceCallbackInfo> liveDataCallbackInfo){
        this.liveDataError = liveDataError;
        this.cacheEntry = cacheEntry;
        this.liveDataCallbackInfo = liveDataCallbackInfo;
    }

    public Throwable getLastError(){
        return liveDataError.getValue();
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if(liveDataCallbackInfo != null){
            WebserviceCallbackInfo callbackInfo = new WebserviceCallbackInfo();
            callbackInfo.headers = response.headers();
            callbackInfo.responseIsSuccessful = response.isSuccessful();
            callbackInfo.responseTime = response.raw().receivedResponseAtMillis() - response.raw().sentRequestAtMillis();
            liveDataCallbackInfo.setValue(callbackInfo);
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
        if(liveDataError != null && t != null){
            liveDataError.setValue(t);
        }
    }

    public void handleResponse(Call<T> call, Response<T> response){
        if(cacheEntry != null){
            cacheEntry.updateValue(response.body());
        }
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
