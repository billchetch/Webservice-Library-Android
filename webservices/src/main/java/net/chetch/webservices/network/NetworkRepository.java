package net.chetch.webservices.network;

import net.chetch.webservices.DataCache;
import net.chetch.webservices.DataStore;
import net.chetch.webservices.Webservice;
import net.chetch.webservices.WebserviceRepository;


public class NetworkRepository extends WebserviceRepository<INetworkService> {

    static private NetworkRepository instance = null;
    static public NetworkRepository getInstance(){
        if(instance == null)instance = new NetworkRepository();
        return instance;
    }

    public NetworkRepository() {
        super(new Webservice(INetworkService.class));
    }


    public DataStore<Services> getServices(){
        DataCache.CacheEntry<Services> entry = cache.getCacheEntry("services", DataCache.FOREVER_CACHE);

        if(entry.requiresUpdating()) {
            service.getServices().enqueue(createCallback(entry));
        }

        return (DataStore)entry;
    }

    public DataStore<ServiceToken> getToken(int serviceID, String clientName){
        final DataStore<ServiceToken> token = new DataStore<>();

        service.getToken(serviceID, clientName).enqueue(createCallback(token));

        return token;
    }

    public DataStore<ServiceToken> saveToken(ServiceToken serviceToken){
        final DataStore<ServiceToken> token = new DataStore<>();

        service.putToken(serviceToken).enqueue(createCallback(token));

        return token;
    }
}
