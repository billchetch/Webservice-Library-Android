package net.chetch.webservices;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.chetch.utilities.DelegateTypeAdapter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

public class DataObjectTypeAdapter extends DelegateTypeAdapter<DataObject>{



    public DataObjectTypeAdapter(){

    }

    @Override
    public boolean isAdapterForType(Type type) {
        boolean canUse = DataObject.class.isAssignableFrom((Class)type);
        return canUse;
    }

    @Override
    public DelegateTypeAdapter<DataObject> useInstance() {
        DataObjectTypeAdapter ta = (DataObjectTypeAdapter) create();
        return ta;
    }

    @Override
    public DataObject read(JsonReader in) throws IOException {
        DataObject dataObject = delegate.read(in);
        dataObject.initialise();
        return dataObject;
    }
}
