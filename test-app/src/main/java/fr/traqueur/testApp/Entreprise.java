package fr.traqueur.testApp;

import java.util.List;

public class Entreprise {
        private final String name;
        private final String address;
        private final List<Person> members;

        public Entreprise(String name, String address, Person... members) {
            this.name = name;
            this.address = address;
            this.members = List.of(members);
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

    public List<Person> getMembers() {
        return members;
    }

    @Override
    public String toString() {
        return "Entreprise{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", members=" + members +
                '}';
    }
}