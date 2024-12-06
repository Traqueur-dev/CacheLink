package fr.traqueur.testApp;

import fr.traqueur.cachelink.CacheConfiguration;
import fr.traqueur.cachelink.CacheFactory;
import fr.traqueur.cachelink.CacheMap;

public class AppSender {

    public static void main(String[] args) throws InterruptedException {
        CacheFactory<String, String> factory =
                new CacheFactory<>(new CacheConfiguration("localhost", 6379, null), String.class, String.class);
        CacheMap<String, String> cache = factory.createCacheMap();
        cache.put("key", "value");
        Thread.sleep(30000);
    }

}
