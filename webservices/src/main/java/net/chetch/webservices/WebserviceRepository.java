package net.chetch.webservices;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import net.chetch.utilities.Utils;
import net.chetch.webservices.employees.Employees;
import net.chetch.webservices.exceptions.WebserviceException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cache;
import okhttp3.Headers;

public class WebserviceRepository<S> {
    static public final int ERROR_SERVICE_UNREACHABLE = 4;

    protected final MutableLiveData<Throwable> liveDataServiceError = new MutableLiveData<>();
    protected final MutableLiveData<Throwable> liveDataRepositoryError = new MutableLiveData<>();
    protected final MutableLiveData<WebserviceCallback.WebserviceCallbackInfo> liveDataCallbackInfo = new MutableLiveData<>();

    protected Webservice<S> webservice;
    protected S service;

    private boolean serviceAvailable = true;
    private Calendar serviceLastAvailable = null;
    private int serviceErrorCode;
    private String serviceErrorMessage;
    private long serverTimeDifference;
    private long serverTimeTolerance =  -1;

    public LiveDataCache cache = new LiveDataCache();

    public WebserviceRepository(Webservice<S> webservice, int defaultCacheTime){
        this.webservice = webservice;

        liveDataServiceError.observeForever(t->{
            handleServiceError(t);
        });

        liveDataCallbackInfo.observeForever(i->{
            handleCallbackInfo(i);
        });

        setDefaultCacheTime(defaultCacheTime);
    }

    public WebserviceRepository(Webservice<S> webservice){
        this(webservice, LiveDataCache.SHORT_CACHE);
    }

    public WebserviceRepository(Class<S> s, int defaultCacheTime){
        this(new Webservice(s), defaultCacheTime);
    }

    public WebserviceRepository(Class<S> s){
        this(new Webservice(s));
    }

    public LiveData<Throwable> getError(){
        return liveDataRepositoryError;
    }

    protected void setError(Throwable t){
        liveDataRepositoryError.setValue(t);
    }

    protected void handleServiceError(Throwable t) {

        if (t instanceof SocketTimeoutException || t instanceof ConnectException || t instanceof UnknownHostException) {
            serviceErrorCode = ERROR_SERVICE_UNREACHABLE;
            serviceAvailable = false;

            //wait a certain time and then reset the serviceAvailable to try again
            serviceLastAvailable = Calendar.getInstance();
            serviceErrorMessage = "Employee unreachable due to " + t.getClass().getName();

        }

        if (t instanceof WebserviceException) {
            WebserviceException wsx = ((WebserviceException) t);
            switch (wsx.getHttpCode()) {
                case 404:
                    serviceAvailable = true;
                    break;

                case 500:
                    serviceAvailable = true;
                    break;

                default:
                    serviceLastAvailable = Calendar.getInstance();
                    serviceAvailable = false;
                    break;

            }
        }

        setError(t);
    }

    private String getHeaderValue(Headers headers, String headerName){
        List<String> values = headers.values(Webservice.HEADER_SERVER_TIME);
        return values != null && values.size() > 0 ? values.get(0) : null;
    }

    protected void handleCallbackInfo(WebserviceCallback.WebserviceCallbackInfo callbackInfo) {
        String dt = getHeaderValue(callbackInfo.headers, Webservice.HEADER_SERVER_TIME);
        if(dt != null){
            try {
                Calendar serverTime = Utils.parseDate(dt, Webservice.DEFAULT_DATE_FORMAT);
                Calendar now = Calendar.getInstance();
                serverTimeDifference = now.getTimeInMillis() - serverTime.getTimeInMillis();
                serverTimeTolerance = callbackInfo.responseTime;

            } catch (Exception e){
                Log.e("WebserviceRespoitory", "handleResponseHeaders " + e.getMessage());
            }
        }
    }

    public boolean isSynchronisedWithServer(int toleranceInSecs){
        if(serverTimeTolerance < 0){ //means no server communication yet
            return false;
        }
        return Math.abs(serverTimeDifference) <= serverTimeTolerance + toleranceInSecs*1000;
    }

    public boolean isSynchronisedWithServer() {
        return isSynchronisedWithServer(0);
    }

    public long getServerTimeDifference(){
        return serverTimeDifference;
    }

    public Calendar getServerTime(){
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(now.getTimeInMillis() - serverTimeDifference);
        return now;
    }

    public void setAPIBaseURL(String apiBaseURL) throws Exception{
        service = webservice.setAPIBaseURL(apiBaseURL);
    }

    public String getAPIBaseURL(){
        return webservice.getAPIBaseURL();
    }

    public WebserviceCallback createCallback(MutableLiveData liveDataResponse){
        return webservice.createCallback(liveDataServiceError, liveDataResponse, liveDataCallbackInfo);
    }

    public WebserviceCallback createCallback(LiveDataCache.CacheEntry cacheEntry){
        return webservice.createCallback(liveDataServiceError, cacheEntry, liveDataCallbackInfo);
    }

    public void setDefaultCacheTime(int cacheTime){
        cache.setDefaultCacheTime(cacheTime);
    }
}
