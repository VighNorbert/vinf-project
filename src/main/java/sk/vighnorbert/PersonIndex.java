package sk.vighnorbert;

import java.util.ArrayList;
import java.util.HashMap;

public class PersonIndex {
    private final HashMap<String, ArrayList<Person>> index;

    public PersonIndex() {
        this.index = new HashMap<>();
    }

    public static String getKey(String name) {
        return name.toLowerCase().strip().split(" ")[0];
    }
    public static String getKey(Person p) {
        return p.getName().toLowerCase().strip().split(" ")[0];
    }

    public void addPerson(Person p) {
        if (index.containsKey(getKey(p))) {
            index.get(getKey(p)).add(p);
        } else {
            ArrayList<Person> list = new ArrayList<>();
            list.add(p);
            index.put(getKey(p), list);
        }
    }

    public Person isPerson(String name) {
        if (index.containsKey(getKey(name))) {
            for (Person p : index.get(getKey(name))) {
                if (name.equals(p.getName())) {
                    return p;
                }
            }
        }
        return null;
    }

    public void runBackCheck() {
        for (ArrayList<Person> list : index.values()) {
            for (Person p : list) {
                if (p instanceof IdentifiedPerson ip) {
                    ip.runBackCheck(this);

                    for (Person parent : ip.getParents()) {
                        if (parent instanceof IdentifiedPerson) {
                            IdentifiedPerson ipParent = (IdentifiedPerson) parent;
                            if (!ipParent.getChildren().contains(ip)) {
                                ipParent.addChild(ip);
                            }
                        }
                    }
                    for (Person child : ip.getChildren()) {
                        if (child instanceof IdentifiedPerson) {
                            IdentifiedPerson ipChild = (IdentifiedPerson) child;
                            if (!ipChild.getParents().contains(ip)) {
                                ipChild.addParent(ip);
                            }
                        }
                    }
                    for (Person spouse : ip.getSpouse()) {
                        if (spouse instanceof IdentifiedPerson) {
                            IdentifiedPerson ipSpouse = (IdentifiedPerson) spouse;
                            if (!ipSpouse.getSpouse().contains(ip)) {
                                ipSpouse.addSpouse(ip);
                            }
                        }
                    }
                }
            }
        }
    }
}
