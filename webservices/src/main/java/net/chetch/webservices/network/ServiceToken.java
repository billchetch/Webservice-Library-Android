package net.chetch.webservices.network;

import net.chetch.webservices.DataObject;

public class ServiceToken extends DataObject {

    public Integer getServiceID(){
        return getCasted("service_id");
    }
    public void setToken(String token){
        setValue("token", token);
    }
    public String getToken(){
        return getCasted("token");
    }
    public String getClientName(){
        return getCasted("client_name");
    }

}
