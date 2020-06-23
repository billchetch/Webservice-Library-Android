package net.chetch.webservices.network;

import net.chetch.webservices.DataObject;

import java.util.Arrays;
import java.util.List;

public class Service extends DataObject {
    public boolean supportsProtocol(String protocol){
        List<String> protocols = Arrays.asList(get("protocols").split(","));
        return protocols.contains(protocol);
    }

    public String getLocalEndpoint(String protocol){
        if(!supportsProtocol(protocol))return null;
        return protocol + "://" + get("lan_ip") + ":" + get("endpoint_port") + "/" + get("endpoint");
    }

    public String getLocalEndpoint(){
        return getLocalEndpoint("http");
    }
}
