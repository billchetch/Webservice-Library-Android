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
    public List<TypeAdapter> typeAdapters;
    public S service;
    private String apiBaseURL;

    public Webservice(Class<S> serviceClass, List<TypeAdapter> typeAdapters){
        this.serviceClass = serviceClass;

        this.typeAdapters = typeAdapters;
    }

    public Webservice(Class<S> serviceClass){
        this.serviceClass = serviceClass;
    }

    public Webservice(Class<S> serviceClass, TypeAdapter typeAdapter){
        this.serviceClass = serviceClass;

        typeAdapters = new ArrayList<>();
        typeAdapters.add(typeAdapter);
    }

    public S setAPIBaseURL(String apiBaseURL) throws Exception{
        this.apiBaseURL = apiBaseURL;
        service = WebserviceManager.addService(apiBaseURL, this);
        return service;
    }

    public <T> WebserviceCallback createCallback(MutableLiveData liveDataError, MutableLiveData<T> liveDataResponse){

        return new WebserviceCallback<T>(liveDataError, liveDataResponse);
    }

}
