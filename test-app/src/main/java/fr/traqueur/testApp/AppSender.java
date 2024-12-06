package fr.traqueur.testApp;

import fr.traqueur.cachelink.CacheConfiguration;
import fr.traqueur.cachelink.CacheFactory;
import fr.traqueur.cachelink.CacheMap;

import java.util.List;

public class AppSender {





    public static void main(String[] args) throws InterruptedException {
        CacheFactory<String, Entreprise> factory =
                new CacheFactory<>(new CacheConfiguration("localhost", 6379, null), String.class, Entreprise.class);
        CacheMap<String, Entreprise> cache = factory.createCacheMap();

        Person person1 = new Person("John", 25);
        Person person2 = new Person("Doe", 30);
        Entreprise entreprise = new Entreprise("Google", "Mountain View", person1, person2);
        cache.put("google", entreprise);
        Thread.sleep(30000);
    }

}
