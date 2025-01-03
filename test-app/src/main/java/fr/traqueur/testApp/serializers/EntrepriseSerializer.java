package fr.traqueur.testApp.serializers;

import fr.traqueur.cachelink.serialization.Serializer;
import fr.traqueur.testApp.Entreprise;

public class EntrepriseSerializer implements Serializer<Entreprise> {

    public static final EntrepriseSerializer INSTANCE = new EntrepriseSerializer();

    @Override
    public String serialize(Entreprise object) {
        return object.getName()+
                "#" + object.getAddress() +
                "#" + object.getMembers()
                .stream()
                .map(PersonSerializer.INSTANCE::serialize)
                .reduce((a, b) -> a + "," + b).orElse("");
    }

    @Override
    public Entreprise deserialize(String object) {
        String[] split = object.split("#");
        String name = split[0];
        String address = split[1];
        String[] members = split[2].split(",");
        return new Entreprise(name, address, PersonSerializer.INSTANCE.deserialize(members[0]), PersonSerializer.INSTANCE.deserialize(members[1]));
    }
}
