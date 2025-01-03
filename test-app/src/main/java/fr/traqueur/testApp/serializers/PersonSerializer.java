package fr.traqueur.testApp.serializers;

import fr.traqueur.cachelink.serialization.Serializer;
import fr.traqueur.testApp.Person;

public class PersonSerializer implements Serializer<Person> {

    public static final PersonSerializer INSTANCE = new PersonSerializer();

    @Override
    public String serialize(Person object) {
        return object.getName() + ";" + object.getAge();
    }

    @Override
    public Person deserialize(String object) {
        String[] split = object.split(";");
        return new Person(split[0], Integer.parseInt(split[1]));
    }
}
