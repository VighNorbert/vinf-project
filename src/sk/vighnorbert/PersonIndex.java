package sk.vighnorbert;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class PersonIndex implements Serializable {
    private final HashMap<String, ArrayList<Person>> index;

    public PersonIndex() {
        this.index = new HashMap<>();
    }

    public static String[] getKey(String name) {
        return name.toLowerCase().strip().split(" ");
    }
    public static String[] getKey(Person p) {
        return p.getName().toLowerCase().strip().split(" ");
    }

    public void addPerson(Person p) {
        for (String k : getKey(p)) {
            if (index.containsKey(k)) {
                index.get(k).add(p);
            } else {
                ArrayList<Person> list = new ArrayList<>();
                list.add(p);
                index.put(k, list);
            }
        }
    }

    public Person isPerson(String name) {
        String k = getKey(name)[0];
        if (index.containsKey(k)) {
            for (Person p : index.get(k)) {
                if (name.equals(p.getName())) {
                    return p;
                }
            }
        }
        return null;
    }

    public ArrayList<Person> getResults(String query) {
        String[] keys = getKey(query);
        ArrayList<Person> results = new ArrayList<>();
        for (String k : keys) {
            if (index.containsKey(k)) {
                for (Person p : index.get(k)) {
                    boolean contains = results.contains(p);
                    if (!contains) {
                        if (query.equals(p.getName())) {
                            results.add(0, p);
                        } else {
                            results.add(p);
                        }
                    }
                }
            }
        }
        return results;
    }

    public void runBackCheck() {
        for (ArrayList<Person> list : index.values()) {
            for (Person p : list) {
                if (p instanceof IdentifiedPerson) {
                    IdentifiedPerson ip = (IdentifiedPerson) p;
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

    public void writeToFile() throws IOException {
        FileWriter fw = new FileWriter("index.csv");

        for (ArrayList<Person> list : index.values()) {
            for (Person p : list) {
                if (p instanceof IdentifiedPerson) {
                    for ( :p.)
                    fw.write(p.getName() + "\n");
                }
            }
        }



        fw.close();
    }
}
