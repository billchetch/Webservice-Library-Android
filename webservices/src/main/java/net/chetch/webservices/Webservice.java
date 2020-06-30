package net.chetch.webservices;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;

import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;

import net.chetch.utilities.DelegateTypeAdapter;
import net.chetch.utilities.DelegateTypeAdapterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class Webservice<S> implements Interceptor{

    static public final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z";
    static public final String DEFAULT_DATE_ONLY_FORMAT = "yyyy-MM-dd Z";
    static public final String DEFAULT_USER_AGENT = "ChetchAndroidWebservice";

    static public final String HEADER_SERVER_TIME = "X-Server-Time";
    static public final String HEADER_DATE_FORMAT = "X-Date-Format";

    public String dateFormat = DEFAULT_DATE_FORMAT;
    public String dateOnlyFormat = DEFAULT_DATE_ONLY_FORMAT;
    public String userAgent = DEFAULT_USER_AGENT;


    public Class<S> serviceClass;
    public List<DelegateTypeAdapter> typeAdapters;
    public DelegateTypeAdapterFactory typeAdapterFactory;
    public S service;
    private String apiBaseURL;

    public Webservice(Class<S> serviceClass, List<DelegateTypeAdapter> typeAdapters){
        this.serviceClass = serviceClass;

        this.typeAdapters = typeAdapters;
    }

    public Webservice(Class<S> serviceClass){
        this.serviceClass = serviceClass;

        typeAdapters = new ArrayList<>();
    }

    public Webservice(Class<S> serviceClass, DelegateTypeAdapter typeAdapter){
        this.serviceClass = serviceClass;

        typeAdapters = new ArrayList<>();
        if(typeAdapter != null) {
            typeAdapters.add(typeAdapter);
        }
    }

    public void addTypeAdapter(DelegateTypeAdapter typeAdapter){
        typeAdapters.add(typeAdapter);
    }

    public String getDefaultName() throws Exception{
        return serviceClass.getDeclaredField("SERVICE_NAME").get(null).toString();
    }

    public S setAPIBaseURL(String apiBaseURL) throws Exception{
        this.apiBaseURL = apiBaseURL;

        typeAdapterFactory = new DelegateTypeAdapterFactory();
        for(DelegateTypeAdapter ta : typeAdapters){
            typeAdapterFactory.addTypeAdapater(ta);
        }

        service = WebserviceManager.addService(apiBaseURL, this);
        return service;
    }

    public String getAPIBaseURL(){
        return this.apiBaseURL;
    }


    public <T> WebserviceCallback<T> createCallback(Observer observer, DataStore<?> dataStore){
        return new WebserviceCallback<T>(observer, dataStore);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        //String noCache = Long.toString(System.currentTimeMillis());

        Request request = original.newBuilder()
                .header("User-Agent", userAgent)
                .method(original.method(), original.body())
                .build();

        okhttp3.Response response = chain.proceed(request);

        return response;
    }
}
