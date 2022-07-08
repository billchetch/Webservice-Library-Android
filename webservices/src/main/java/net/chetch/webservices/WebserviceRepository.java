package net.chetch.webservices;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import android.util.Log;

import net.chetch.utilities.SLog;
import net.chetch.utilities.Utils;
import net.chetch.webservices.exceptions.WebserviceException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.List;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import retrofit2.Response;

public class WebserviceRepository<S> implements Observer{
    static public final int ERROR_SERVICE_UNREACHABLE = 4;

    protected MutableLiveData<Throwable> liveDataError = new MutableLiveData<>();

    protected Webservice<S> webservice;
    protected S service;
    protected DataObjectTypeAdapter dataObjectTypeAdapter = new DataObjectTypeAdapter();
    protected DataFieldTypeAdapter dataFieldValueTypeAdapter = new DataFieldTypeAdapter();

    private boolean serviceAvailable = true;
    private Calendar serviceLastAvailable = null;
    private int serviceErrorCode;
    private String serviceErrorMessage;
    private long serverTimeDifference;
    private long serverTimeTolerance =  -1;

    protected DataCache cache = new DataCache();

    public WebserviceRepository(Webservice<S> webservice, int defaultCacheTime){
        this.webservice = webservice;

        if(dataFieldValueTypeAdapter.defaultDateFormat == null){
            dataFieldValueTypeAdapter.defaultDateFormat = webservice.dateFormat;
        }

        this.webservice.addTypeAdapter(dataObjectTypeAdapter);
        this.webservice.addTypeAdapter(dataFieldValueTypeAdapter);
        setDefaultCacheTime(defaultCacheTime);
    }

    public WebserviceRepository(Webservice<S> webservice){
        this(webservice, DataCache.SHORT_CACHE);
    }

    public WebserviceRepository(Class<S> s, int defaultCacheTime){
        this(new Webservice(s), defaultCacheTime);
    }

    public WebserviceRepository(Class<S> s){
        this(new Webservice(s));
    }

    @Override
    public void onChanged(Object o) {
        if(o instanceof Throwable){
            handleServiceError((Throwable)o);
        } else if(o instanceof WebserviceCallback.WebserviceCallbackInfo){
            handleCallbackInfo((WebserviceCallback.WebserviceCallbackInfo)o);
        } else if(o instanceof Response){
            handleResponse((Response)o);
        }
    }

    public LiveData<Throwable> getError(){
        return liveDataError;
    }

    protected void setError(Throwable t){
        if(t instanceof WebserviceException){
            ((WebserviceException)t).setServiceAvailable(serviceAvailable);
        }

        liveDataError.postValue(t);
    }

    public boolean isServiceAvailable(){
        return serviceAvailable;
    }

    protected void handleServiceError(Throwable t) {
        if (t instanceof WebserviceException) {
            WebserviceException wsx = ((WebserviceException) t);
            wsx.setTag(this);

            Throwable origThrow = wsx.getThrowable();
            if(origThrow != null){
                if (origThrow instanceof SocketTimeoutException || origThrow instanceof ConnectException || origThrow instanceof UnknownHostException) {
                    serviceErrorCode = ERROR_SERVICE_UNREACHABLE;
                    serviceAvailable = false;

                    //wait a certain time and then reset the serviceAvailable to try again
                    serviceLastAvailable = Calendar.getInstance();
                    serviceErrorMessage = "Service unreachable due to " + origThrow.getClass().getName();
                    t = new WebserviceException(serviceErrorMessage, serviceErrorCode, t);
                    ((WebserviceException)t).setRequest(wsx.getRequest());
                    ((WebserviceException)t).setTag(this);
                }
            } else {
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

            if(!serviceAvailable && wsx.getDataStore() != null && wsx.getDataStore() instanceof DataCache.CacheEntry){
                ((DataCache.CacheEntry)wsx.getDataStore()).forceExpire();
            }

        } // end of test if WebserviceException
        else {
            if(SLog.LOG)SLog.w("WSR", "WebserviceRepository::handleServiceError handling non WebserviceException exception " + (t == null ?  "[null]" :  t.getClass()));
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
                serverTimeTolerance = callbackInfo.responseTime / 2;
                serverTimeDifference = serverTime.getTimeInMillis() + serverTimeTolerance - now.getTimeInMillis();

            } catch (Exception e){
                if(SLog.LOG)SLog.e("WebserviceRespoitory", "handleResponseHeaders " + e.getMessage());
            }
        }
    }

    protected void handleResponse(Response response){
        //stub to overwrite if required
    }

    public void synchronise(WebserviceRepository otherRepo){
        serverTimeDifference = otherRepo.getServerTimeDifference();
        serverTimeTolerance = otherRepo.getServerTimeTolerance();
        dataFieldValueTypeAdapter.adjustForServerTimeDifference = otherRepo.dataFieldValueTypeAdapter.adjustForServerTimeDifference;
    }

    public long getServerTimeDifference(){
        return serverTimeDifference;
    }

    public long getServerTimeTolerance(){
        return serverTimeTolerance;
    }

    public Calendar getServerTime(){
        return getServerTime(Calendar.getInstance());
    }

    public Calendar getServerTime(Calendar localCal){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(localCal.getTimeInMillis() + serverTimeDifference);
        return c;
    }

    public boolean isSynchronisedWithServer(int toleranceInSecs){
        return Math.abs(serverTimeDifference) <= serverTimeTolerance + toleranceInSecs*1000;
    }

    public void adjustForServerTimeDifference(boolean adjust){
        dataFieldValueTypeAdapter.adjustForServerTimeDifference = adjust;
    }

    public void setAPIBaseURL(String apiBaseURL) throws Exception{
        service = webservice.setAPIBaseURL(apiBaseURL);
    }

    public String getAPIBaseURL(){
        return webservice.getAPIBaseURL();
    }

    public String getAPIStub(String url){
        return webservice.getAPIStub(url);
    }

    public <T> WebserviceCallback<T> createCallback(DataStore dataStore){
        return webservice.createCallback(this, dataStore);
    }

    public <T> DataCache.CacheEntry<T> getCacheEntry(String key){
        return cache.getCacheEntry(key);
    }

    public void setDefaultCacheTime(int cacheTime){
        cache.setDefaultCacheTime(cacheTime);
    }
}
