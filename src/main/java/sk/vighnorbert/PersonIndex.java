package sk.vighnorbert;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class holding the index of all the parsed people from the Wikipedia dump.
 *
 * @author Norbert Vígh
 */
public class PersonIndex implements Serializable {

    /**
     * The index of all the people itself
     */
    private final HashMap<String, ArrayList<IdentifiedPerson>> index;

    public PersonIndex() {
        this.index = new HashMap<>();
    }

    /**
     * Static function to get all the keys to the name
     *
     * @param name name to get the keys for
     * @return the array of keys to the name
     */
    public static String[] getKey(String name) {
        return name.toLowerCase().trim().split(" ");
    }

    /**
     * Static function to get all the keys to the person
     *
     * @param p person to get the keys for
     * @return the array of keys to the person
     */
    public static String[] getKey(Person p) {
        return p.getName().toLowerCase().trim().split(" ");
    }

    /**
     * A function used to add a person to the index
     *
     * @param p person to add
     */
    public void addPerson(IdentifiedPerson p) {
        System.out.println("adding person " + p.getName());
        for (String k : getKey(p)) {
            if (index.containsKey(k)) {
                // add to the existing list
                index.get(k).add(p);
            } else {
                // create a new list and add it to that list
                ArrayList<IdentifiedPerson> list = new ArrayList<>();
                list.add(p);
                index.put(k, list);
            }
        }
    }

    /**
     * Function for debug output of the size of the index
     */
    public void out() {
        System.out.println("size: " + index.values().size());
    }

    /**
     * Function to check if a person is in the index and therefore is a person,
     * since all people are in the index after parsing.
     *
     * @param name name of the person to check
     * @return the person if found, null otherwise
     */
    public Person isPerson(String name) {
        String k = getKey(name)[0];
        // if the first key is not in the index, the person is not in the index
        if (index.containsKey(k)) {
            for (Person p : index.get(k)) {
                // check if the person is the same
                if (name.equals(p.getName())) {
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * Function to get all the people with a name matching the given query
     *
     * @param query the query to search by
     * @return the list of people matching the query
     */
    public ArrayList<Person> getResults(String query) {
        String[] keys = getKey(query);
        ArrayList<Person> results = new ArrayList<>();
        for (String k : keys) {
            if (index.containsKey(k)) {
                // add all the people with the key to the results
                for (Person p : index.get(k)) {
                    boolean contains = results.contains(p);
                    if (!contains) {
                        // if it is an exact match, add it to the beginning of the list
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

    /**
     * Function to run back-check on all people in the index
     */
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

    /**
     * Function to serialize the index to a file
     *
     * @throws IOException if the file cannot be written
     */
    public void writeToFile() throws IOException {
        // create the file to write to
        FileWriter fw = new FileWriter("extracted-data.out");

        for (ArrayList<IdentifiedPerson> list : index.values()) {
            for (Person p : list) {
                if (p.serialized) {
                    // already done
                    continue;
                }
                System.out.println("Writing " + p.getName());
                if (p instanceof IdentifiedPerson) {
                    IdentifiedPerson ip = (IdentifiedPerson) p;

                    // serialize all the relative categories per all the people
                    fw.write(serializeCollection(ip.getSpouse(), "spouse", ip.getName()));
                    fw.write(serializeCollection(ip.getParents(), "parent", ip.getName()));
                    fw.write(serializeCollection(ip.getChildren(), "child", ip.getName()));
                }
                // mark the person as already serialized
                p.serialized = true;
            }
        }

        // save the file safely
        fw.close();
    }

    /**
     * Function to deserialize the index from a file
     *
     * @throws IOException if the file cannot be read
     */
    public void readFromFile() throws IOException {
        try {
            // open the file to read from
            BufferedReader br = new BufferedReader(new FileReader("extracted-data.out"));

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                String name = parts[0];
                String type = parts[1];

                // check if the person already exists
                IdentifiedPerson p = (IdentifiedPerson) isPerson(name);
                if (p == null) {
                    p = new IdentifiedPerson(name);
                    addPerson(p);
                }

                // for all the people in the line, add them to the appropriate list
                for (int i = 2; i < parts.length; i++) {
                    String name2 = parts[i];
                    Person p2;

                    if (name2.charAt(0) == '?') {
                        // add an unknown person
                        name2 = name2.substring(1);
                        p2 = new Person(name2);
                    } else {
                        // add an identified person
                        p2 = isPerson(name2);
                        if (p2 == null) {
                            p2 = new IdentifiedPerson(name2);
                            addPerson((IdentifiedPerson) p2);
                        }
                    }

                    // according to the type, add the person to the right category
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

    /**
     * Function to serialize a collection of people to one line format
     *
     * @param collection the collection to serialize
     * @param key        the key to use for the line
     * @param ipName     the name of the person which is being serialized
     * @return the serialized line
     */
    public String serializeCollection(ArrayList<Person> collection, String key, String ipName) {
        if (collection.size() > 0) {
            StringBuilder s = new StringBuilder(ipName + "\t" + key);
            // append all the people in the collection
            for (Person item : collection) {
                if (item instanceof IdentifiedPerson) {
                    // add an identified person
                    s.append("\t").append(item.getName());
                } else {
                    // add an unknown person
                    s.append("\t?").append(item.getName());
                }
            }
            s.append("\n");
            return s.toString();
        }
        // if the collection is empty, no need to serialize
        return "";
    }
}
