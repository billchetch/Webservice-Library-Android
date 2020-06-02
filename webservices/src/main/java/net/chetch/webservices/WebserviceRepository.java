package net.chetch.webservices;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import net.chetch.webservices.exceptions.WebserviceException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Calendar;

public class WebserviceRepository<S> {
    public static final int ERROR_SERVICE_UNREACHABLE = 4;


    protected final MutableLiveData<Throwable> liveDataServiceError = new MutableLiveData<>();
    protected final MutableLiveData<Throwable> liveDataRepositoryError = new MutableLiveData<>();

    protected Webservice<S> webservice;
    protected S service;

    private boolean serviceAvailable = true;
    private Calendar serviceLastAvailable = null;
    private int serviceErrorCode;
    private String serviceErrorMessage;

    public WebserviceRepository(Webservice<S> webservice){
        this.webservice = webservice;

        liveDataServiceError.observeForever(t->{
            handleServiceError(t);
        });
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
            serviceErrorMessage = "Service unreachable due to " + t.getClass().getName();

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

    public void setAPIBaseURL(String apiBaseURL) throws Exception{
        service = webservice.setAPIBaseURL(apiBaseURL);
    }

    public <T> WebserviceCallback createCallback(MutableLiveData<T> liveDataResponse){
        return webservice.createCallback(liveDataServiceError, liveDataResponse);
    }
}
