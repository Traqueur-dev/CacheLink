package fr.traqueur.testApp;

import java.util.List;

public class Entreprise {
        private String name;
        private String address;
        private List<Person> members;

        public Entreprise() {

        }

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

        @Override
        public String toString() {
            return "Entreprise{" +
                    "name='" + name + '\'' +
                    ", address='" + address + '\'' +
                    ", members=" + members +
                    '}';
        }
    }