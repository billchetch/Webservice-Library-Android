package net.chetch.webservices.network;

import net.chetch.webservices.DataObject;

import java.util.Arrays;
import java.util.List;

public class Service extends DataObject {
    public boolean supportsProtocol(String protocol){
        List<String> protocols = Arrays.asList(getValue("protocols").toString().split(","));
        return protocols.contains(protocol);
    }

    public String getLocalEndpoint(String protocol){
        if(!supportsProtocol(protocol))return null;
        return protocol + "://" + getValue("lan_ip") + ":" + getValue("endpoint_port") + "/" + getValue("endpoint");
    }

    public String getLocalEndpoint(){
        return getLocalEndpoint("http");
    }
}
