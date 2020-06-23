package net.chetch.webservices.network;

import java.util.HashMap;

public class Services extends HashMap<String, Service> {


    public Service getService(String serviceName){
        if(containsKey(serviceName)){
            return get(serviceName);
        } else {
            return null;
        }
    }

    public boolean hasService(String serviceName){
        return getService(serviceName) != null;
    }

    public String getServiceLocalEndpoint(String serviceName, String protocol){
        Service service = getService(serviceName);
        if(service != null){
            return service.getLocalEndpoint(protocol);
        } else {
            return null;
        }
    }

    public String getServiceLocalEndpoint(String serviceName) {
        return getServiceLocalEndpoint(serviceName, "http");
    }
}
