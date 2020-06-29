package net.chetch.webservices;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.chetch.utilities.DelegateTypeAdapter;
import net.chetch.utilities.Utils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Response;

public class DataObjectTypeAdapter extends DelegateTypeAdapter<DataObject> implements Interceptor{

    private String getHeaderValue(Headers headers, String headerName){
        List<String> values = headers.values(headerName);
        return values != null && values.size() > 0 ? values.get(0) : null;
    }

    public long serverTimeDifference = 0;
    public boolean adjustForServerTimeDifference = false;
    public Type type;

    public DataObjectTypeAdapter(){

    }

    @Override
    public boolean isAdapterForType(Type type) {
        boolean canUse = DataObject.class.isAssignableFrom((Class)type);
        return canUse;
    }

    @Override
    public DelegateTypeAdapter<DataObject> newInstance() {
        DataObjectTypeAdapter ta = (DataObjectTypeAdapter)create();
        ta.adjustForServerTimeDifference = adjustForServerTimeDifference;
        return ta;
    }

    @Override
    public DataObject read(JsonReader in) throws IOException {
        DataObject dataObject = delegate.read(in);
        dataObject.setServerTimeDifference(adjustForServerTimeDifference, serverTimeDifference);
        return dataObject;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        okhttp3.Response response = chain.proceed(chain.request());

        try {
            String dt = getHeaderValue(response.headers(), Webservice.HEADER_SERVER_TIME);
            if (dt != null) {
                Calendar serverTime = Utils.parseDate(dt, Webservice.DEFAULT_DATE_FORMAT);
                Calendar now = Calendar.getInstance();
                long responseTime = (response.receivedResponseAtMillis() - response.sentRequestAtMillis()) / 2;
                serverTimeDifference = serverTime.getTimeInMillis() + responseTime - now.getTimeInMillis();
            }
        } catch (Exception e){
            Log.e("DOTA", e.getMessage());
        }

        return response;
    }
}
