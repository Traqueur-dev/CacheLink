package fr.traqueur.testApp;

import fr.traqueur.cachelink.CacheConfiguration;
import fr.traqueur.cachelink.CacheFactory;
import fr.traqueur.cachelink.CacheMap;

public class AppReceiver {

    public static void main(String[] args) {
        CacheFactory<String, Entreprise> factory =
                new CacheFactory<>(new CacheConfiguration("localhost", 6379, null), String.class, Entreprise.class);
        CacheMap<String, Entreprise> cache = factory.createCacheMap();
        System.out.println(cache.get("google"));
    }

}
