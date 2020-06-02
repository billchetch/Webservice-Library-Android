package net.chetch.webservices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;

import net.chetch.utilities.CalendarTypeAdapater;
import net.chetch.utilities.DelegateTypeAdapterFactory;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WebserviceManager {
    static private HashMap<String, Object> services = new HashMap<>();
    static private HashMap<String, OkHttpClient> clients = new HashMap<>();

    static public <S> S addService(String apiBaseURL, Webservice<S> ws) throws Exception {
        if(apiBaseURL == null) {
          throw new Exception("API Base URL cannot be null");
        }

        if(!apiBaseURL.endsWith("/"))apiBaseURL += "/";

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            //String noCache = Long.toString(System.currentTimeMillis());

            Request request = original.newBuilder()
                    .header("User-Agent", ws.userAgent)
                    .method(original.method(), original.body())
                    .build();

            return chain.proceed(request);
        });


        OkHttpClient client = httpClient.build();

        DelegateTypeAdapterFactory delegateTypeAdapterFactory = new DelegateTypeAdapterFactory();

        //delegateTypeAdapterFactory.addTypeAdapater(ForecastTypeAdapater.class);

        Gson gson = new GsonBuilder()
                .setDateFormat(ws.dateFormat)
                .registerTypeAdapter(Calendar.class, new CalendarTypeAdapater(ws.dateFormat))
                .registerTypeAdapterFactory(delegateTypeAdapterFactory)
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(apiBaseURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        S service = retrofit.create(ws.serviceClass);

        services.put(apiBaseURL, service);
        clients.put(apiBaseURL, client);

        return service;
    }


    static public void cancelAllCalls(String apiBaseURL){
        OkHttpClient client = clients.get(apiBaseURL);
        if(client != null){
            client.dispatcher().cancelAll();
        }
    }

    static public <S> S getService(String apiBaseURL){
        if(apiBaseURL == null)throw new Error("API Base URL cannot be NULL");

        return (S)services.get(apiBaseURL);
    }
}
