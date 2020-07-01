package net.chetch.webservices;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.chetch.utilities.DelegateTypeAdapter;
import net.chetch.utilities.Utils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Response;

public class DataFieldTypeAdapter extends DelegateTypeAdapter<DataField> implements Interceptor{

    private Pattern numericPattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    protected enum NumberType {
        INTEGER,
        DOUBLE,
        NAN
    }

    protected TypeAdapter<Object> objectTypeAdapter;

    public String defaultDateFormat;
    public long serverTimeDifference = 0;
    public boolean adjustForServerTimeDifference = false;

    @Override
    public boolean isAdapterForType(Type type) {
        return type.equals(DataField.class);
    }

    @Override
    public void setDelegate(Gson gson, TypeAdapter<DataField> delegate) {
        objectTypeAdapter = gson.getAdapter(Object.class);
        super.setDelegate(gson, delegate);
    }

    protected NumberType isNumeric(String strNum) {
        if (strNum == null) {
            return NumberType.NAN;
        }
        if(numericPattern.matcher(strNum).matches()){
            return strNum.contains(".") ? NumberType.DOUBLE : NumberType.INTEGER;

        } else {
            return NumberType.NAN;
        }
    }

    public boolean checkSplit(String s, String delimiter, int partsCount, int ... lengths){
        String[] parts = s.split(delimiter);
        if(parts.length != partsCount)return false;

        for(int i = 0; i < lengths.length; i++){
            if(parts[i].length() != lengths[i])return false;
        }
        return true;
    }

    public boolean isPossibleDate(String strDate){
        if(strDate == null || strDate.length() == 0)return false;

        String[] parts;
        String delimiter;
        switch(defaultDateFormat){
            case "yyyy-MM-dd HH:mm:ss":
                delimiter = " ";
                if(!checkSplit(strDate, delimiter, 2, 10, 8))return false;
                parts = strDate.split(delimiter);

                if(!checkSplit(parts[0], "-", 3,4,2,2))return false;
                if(!checkSplit(parts[1], ":", 3,2,2,2))return false;
                return true;

            case "yyyy-MM-dd HH:mm:ss Z":
                delimiter = " ";
                if(!checkSplit(strDate, delimiter, 3,10, 8))return false;
                parts = strDate.split(delimiter);

                if(!checkSplit(parts[0], "-", 3,4,2,2))return false;
                if(!checkSplit(parts[1], ":", 3,2,2,2))return false;
                return true;

            default:
                return false;
        }
    }

    @Override
    public DataField read(JsonReader in) throws IOException {
        Object obj;
        switch(in.peek()){
            case NUMBER:
                obj = in.nextString(); //to avoid automatic doubles
                break;

            default:
                obj = objectTypeAdapter.read(in);
        }
        Object value = null;
        if(obj instanceof String){
            String valueAsString = obj.toString();
            NumberType nt = isNumeric(valueAsString);
            switch(nt){
                case INTEGER:
                    value = Integer.parseInt(valueAsString);
                    break;

                case DOUBLE:
                    value = Double.parseDouble(valueAsString);
                    break;

                case NAN:
                    if(isPossibleDate(valueAsString)){
                        SimpleDateFormat f = new SimpleDateFormat(defaultDateFormat);
                        Calendar cal = Calendar.getInstance();
                        try {
                            cal.setTime(f.parse(valueAsString));
                            if(adjustForServerTimeDifference) {
                                cal.setTimeInMillis(cal.getTimeInMillis() - serverTimeDifference);
                            }
                            value = cal;
                        } catch (Exception e) {
                            Log.e("DFTA", e.getMessage());
                            value = valueAsString;
                        }
                    } else {
                        value = valueAsString;
                    }
                    break;
            }

        } else {
            value = obj;
        }

        DataField field = new DataField();
        field.setValue(value);
        return field;
    }

    @Override
    public void write(JsonWriter out, DataField value) throws IOException {
        Object value2write = value.getValue();

        objectTypeAdapter.write(out, value2write);
    }

    private String getHeaderValue(Headers headers, String headerName){
        List<String> values = headers.values(headerName);
        return values != null && values.size() > 0 ? values.get(0) : null;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        okhttp3.Response response = chain.proceed(chain.request());

        try {
            String headerDateFormat = getHeaderValue(response.headers(), Webservice.HEADER_DATE_FORMAT);
            if(headerDateFormat == null)headerDateFormat = Webservice.DEFAULT_DATE_FORMAT;
            if(defaultDateFormat == null)defaultDateFormat = headerDateFormat;

            String dt = getHeaderValue(response.headers(), Webservice.HEADER_SERVER_TIME);
            if (dt != null) {
                Calendar serverTime = Utils.parseDate(dt, headerDateFormat);
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
