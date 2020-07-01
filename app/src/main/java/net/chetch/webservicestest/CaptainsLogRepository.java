package net.chetch.webservicestest;

import net.chetch.webservices.AboutService;
import net.chetch.webservices.DataCache;
import net.chetch.webservices.DataStore;
import net.chetch.webservices.WebserviceRepository;

public class CaptainsLogRepository extends WebserviceRepository<ICaptainsLogService> {
    static public int ENTRIES_PAGE_SIZE = 250;

    static private CaptainsLogRepository instance = null;
    static public CaptainsLogRepository getInstance(){
        if(instance == null)instance = new CaptainsLogRepository();
        return instance;
    }

    public CaptainsLogRepository(){
        this(DataCache.VERY_SHORT_CACHE);
    }
    public CaptainsLogRepository(int defaultCacheTime){
        super(ICaptainsLogService.class, defaultCacheTime);
    }

    public DataStore<AboutService> getAbout(){
        DataCache.CacheEntry<AboutService> entry = cache.getCacheEntry("about-service");

        if(entry.requiresUpdating()) {
            service.getAbout().enqueue(createCallback(entry));
        }

        return entry;
    }

    public DataStore<CrewStats> getCrewStats(){
        DataCache.CacheEntry<CrewStats> entry = cache.getCacheEntry("crew-stats");

        if(entry.requiresUpdating()) {
            service.getCrewStats().enqueue(createCallback(entry));
        }

        return entry;
    }
}
