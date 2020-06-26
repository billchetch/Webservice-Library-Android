package net.chetch.webservices;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
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
import retrofit2.Response;

public class WebserviceRepository<S> implements Observer{
    static public final int ERROR_SERVICE_UNREACHABLE = 4;

    protected MutableLiveData<Throwable> liveDataError = new MutableLiveData<>();

    protected Webservice<S> webservice;
    protected S service;

    private boolean serviceAvailable = true;
    private Calendar serviceLastAvailable = null;
    private int serviceErrorCode;
    private String serviceErrorMessage;
    private long serverTimeDifference;
    private long serverTimeTolerance =  -1;

    public DataCache cache = new DataCache();

    public WebserviceRepository(Webservice<S> webservice, int defaultCacheTime){
        this.webservice = webservice;

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

        liveDataError.setValue(t);
    }

    protected void handleServiceError(Throwable t) {
        WebserviceException wsx = null;

        if (t instanceof SocketTimeoutException || t instanceof ConnectException || t instanceof UnknownHostException) {
            serviceErrorCode = ERROR_SERVICE_UNREACHABLE;
            serviceAvailable = false;

            //wait a certain time and then reset the serviceAvailable to try again
            serviceLastAvailable = Calendar.getInstance();
            serviceErrorMessage = "Service unreachable due to " + t.getClass().getName();
            wsx = new WebserviceException(serviceErrorMessage, serviceErrorCode, t);
            setError(wsx);
            return;
        }

        if (t instanceof WebserviceException) {
            wsx = ((WebserviceException) t);
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

    protected void handleResponse(Response response){
        //do nothing atm .. can be overriden if required
    }

    public void synchronise(WebserviceRepository otherRepo){
        serverTimeDifference = otherRepo.getServerTimeDifference();
        serverTimeTolerance = otherRepo.getServerTimeTolerance();
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

    public long getServerTimeTolerance(){
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
        return webservice.createCallback(this, liveDataResponse);
    }

    public WebserviceCallback createCallback(DataCache.CacheEntry cacheEntry){
        return webservice.createCallback(this, cacheEntry);
    }

    public void setDefaultCacheTime(int cacheTime){
        cache.setDefaultCacheTime(cacheTime);
    }


}
