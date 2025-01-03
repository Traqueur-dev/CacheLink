package fr.traqueur.testApp;

import fr.traqueur.cachelink.CacheConfiguration;
import fr.traqueur.cachelink.CacheFactory;
import fr.traqueur.cachelink.collections.CacheMap;
import fr.traqueur.cachelink.serialization.Serializer;
import fr.traqueur.testApp.serializers.EntrepriseSerializer;

public class AppReceiver {

    public static void main(String[] args) {
        CacheFactory.init(new CacheConfiguration("localhost", 6379, null));
        CacheMap<String, Entreprise> cache = new CacheMap<>("entreprises",
                Serializer.STRING, EntrepriseSerializer.INSTANCE);
        System.out.println(cache.get("google"));
    }

}
