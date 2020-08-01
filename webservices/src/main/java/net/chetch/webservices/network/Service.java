package net.chetch.webservices.network;

import android.util.Log;

import net.chetch.webservices.DataObject;

import java.util.Arrays;
import java.util.List;

public class Service extends DataObject {


    @Override
    public void initialise() {
        super.initialise();
        asInteger("endpoint_port");
    }


    public boolean supportsProtocol(String protocol){
        List<String> protocols = Arrays.asList(getValue("protocols").toString().split(","));
        return protocols.contains(protocol);
    }

    public String getDomain(){
        return getCasted("domain");
    }

    public String getLanIP(){
        return getCasted("lan_ip");
    }

    public String getEndpoint(){
        return getCasted("endpoint");
    }


    public Integer getEndpointPort(){
        return getCasted("endpoint_port");
    }

    public String getLocalEndpoint(String protocol){
        if(!supportsProtocol(protocol))return null;

        String localEndpoint = protocol;
        String domain = getDomain() != null ? getValue("domain").toString() : getValue("lan_ip").toString();
        localEndpoint += "://" + domain;
        if(getEndpointPort() != null){
            localEndpoint += ":" + getEndpointPort();
        }
        localEndpoint += "/" + getEndpoint();
        return localEndpoint;
    }

    public String getLocalEndpoint(){
        return getLocalEndpoint("http");
    }
}
