package fr.traqueur.testApp;

import fr.traqueur.cachelink.CacheConfiguration;
import fr.traqueur.cachelink.CacheFactory;
import fr.traqueur.cachelink.collections.CacheMap;
import fr.traqueur.cachelink.serialization.Serializer;
import fr.traqueur.testApp.serializers.EntrepriseSerializer;

public class AppSender {





    public static void main(String[] args) throws InterruptedException {
        CacheFactory.init(new CacheConfiguration("localhost", 6379, null));
        CacheMap<String, Entreprise> cache = new CacheMap<>("entreprises",
                Serializer.STRING, EntrepriseSerializer.INSTANCE);

        Person person1 = new Person("John", 25);
        Person person2 = new Person("Doe", 30);
        Entreprise entreprise = new Entreprise("Google", "Mountain View", person1, person2);
        cache.put("google", entreprise);
        Thread.sleep(30000);
    }

}
