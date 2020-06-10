package net.chetch.webservices;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.google.gson.TypeAdapter;

import java.util.ArrayList;
import java.util.List;

public class Webservice<S> {

    public final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z";
    public final String DEFAULT_DATE_ONLY_FORMAT = "yyyy-MM-dd Z";
    public final String DEFAULT_USER_AGENT = "ChetchAndroidWebservice";

    public String dateFormat = DEFAULT_DATE_FORMAT;
    public String dateFormatOnly = DEFAULT_DATE_ONLY_FORMAT;
    public String userAgent = DEFAULT_USER_AGENT;


    public Class<S> serviceClass;
    public List<Class<TypeAdapter>> typeAdapterClasses;
    public S service;
    private String apiBaseURL;

    public Webservice(Class<S> serviceClass, List<Class<TypeAdapter>> typeAdapterClasses){
        this.serviceClass = serviceClass;

        this.typeAdapterClasses = typeAdapterClasses;
    }

    public Webservice(Class<S> serviceClass){
        this.serviceClass = serviceClass;
    }

    public Webservice(Class<S> serviceClass, Class<TypeAdapter> typeAdapterClass){
        this.serviceClass = serviceClass;

        typeAdapterClasses = new ArrayList<>();
        typeAdapterClasses.add(typeAdapterClass);
    }

    public S setAPIBaseURL(String apiBaseURL) throws Exception{
        this.apiBaseURL = apiBaseURL;
        service = WebserviceManager.addService(apiBaseURL, this);
        return service;
    }

    public String getAPIBaseURL(){
        return this.apiBaseURL;
    }

    public WebserviceCallback createCallback(MutableLiveData liveDataError, MutableLiveData liveDataResponse){

        return new WebserviceCallback(liveDataError, liveDataResponse);
    }

    public WebserviceCallback createCallback(MutableLiveData liveDataError, LiveDataCache.CacheEntry cacheEntry){

        return new WebserviceCallback(liveDataError, cacheEntry);
    }

}
