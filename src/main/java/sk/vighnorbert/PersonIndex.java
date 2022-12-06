package sk.vighnorbert;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class PersonIndex implements Serializable {
    private final HashMap<String, ArrayList<IdentifiedPerson>> index;

    public PersonIndex() {
        this.index = new HashMap<>();
    }

    public static String[] getKey(String name) {
        return name.toLowerCase().trim().split(" ");
    }
    public static String[] getKey(Person p) {
        return p.getName().toLowerCase().trim().split(" ");
    }

    public void addPerson(IdentifiedPerson p) {
        System.out.println("adding person " + p.getName());
        for (String k : getKey(p)) {
            if (index.containsKey(k)) {
                index.get(k).add(p);
            } else {
                ArrayList<IdentifiedPerson> list = new ArrayList<>();
                list.add(p);
                index.put(k, list);
            }
        }
    }
    public void out() {
        System.out.println("size: " + index.values().size());
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
        for (ArrayList<IdentifiedPerson> list : index.values()) {
            for (Person p : list) {
                if (p instanceof IdentifiedPerson) {
                    IdentifiedPerson ip = (IdentifiedPerson) p;
                    ip.runBackCheck(this);
                }
            }
        }
    }

    public void writeToFile() throws IOException {
        FileWriter fw = new FileWriter("extracted-data.out");

        for (ArrayList<IdentifiedPerson> list : index.values()) {
            for (Person p : list) {
                if (p.serialized) {
                    continue;
                }
                System.out.println("Writing " + p.getName());
                if (p instanceof IdentifiedPerson) {
                    IdentifiedPerson ip = (IdentifiedPerson) p;

                    fw.write(serializeCollection(ip.getSpouse(), "spouse", ip.getName()));
                    fw.write(serializeCollection(ip.getParents(), "parent", ip.getName()));
                    fw.write(serializeCollection(ip.getChildren(), "child", ip.getName()));
                }
                p.serialized = true;
            }
        }

        fw.close();
    }

    public void readFromFile() throws IOException {
        try {
            BufferedReader br = new BufferedReader(new FileReader("extracted-data.out"));

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                String name = parts[0];
                String type = parts[1];

                IdentifiedPerson p = (IdentifiedPerson) isPerson(name);
                if (p == null) {
                    p = new IdentifiedPerson(name);
                    addPerson(p);
                }

                for (int i = 2; i < parts.length; i++) {
                    String name2 = parts[i];
                    Person p2;

                    if (name2.charAt(0) == '?') {
                        name2 = name2.substring(1);
                        p2 = new Person(name2);
                    } else {
                        p2 = isPerson(name2);
                        if (p2 == null) {
                            p2 = new IdentifiedPerson(name2);
                            addPerson((IdentifiedPerson) p2);
                        }
                    }

                    switch (type) {
                        case "spouse":
                            p.addSpouse(p2);
                            break;
                        case "parent":
                            p.addParent(p2);
                            break;
                        case "child":
                            p.addChild(p2);
                            break;
                    }
                }
            }

            br.close();
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: File \"extracted-data.out\" not found! Run the parse command to extract it first.\n");
        }
    }

    private String serializeCollection(ArrayList<Person> collection, String key, String ipName) {
        if (collection.size() > 0) {
            StringBuilder s = new StringBuilder(ipName + "\t" + key);
            for (Person item : collection) {
                if (item instanceof IdentifiedPerson) {
                    s.append("\t").append(item.getName());
                } else {
                    s.append("\t?").append(item.getName());
                }
            }
            s.append("\n");
            return s.toString();
        }
        return "";
    }
}
