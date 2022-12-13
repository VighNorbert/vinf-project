package sk.vighnorbert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for a person which is sure to be a person from the Wikipedia dump.
 *
 * @author Norbert Vígh
 */
public class IdentifiedPerson extends Person {

    /**
     * Max distance to the Named entity
     */
    private final static int MAX_DISTANCE = 10;

    /**
     * Object holding all the potential matches
     */
    private MatchesHolder mh;

    /**
     * Array of all the sure parents
     */
    private final ArrayList<Person> parents;

    /**
     * Array of all the sure children
     */
    private final ArrayList<Person> children;

    /**
     * Array of all the sure spouses
     */
    private final ArrayList<Person> spouse;


    public IdentifiedPerson(String name) {
        super(name);
        this.parents = new ArrayList<>();
        this.children = new ArrayList<>();
        this.spouse = new ArrayList<>();
        this.mh = new MatchesHolder();
    }

    /**
     * Getter for the object holding all the unconfirmed matches
     *
     * @return the object holding all the unconfirmed matches
     */
    public MatchesHolder getMh() {
        return mh;
    }

    /**
     * Getter for the name of the person
     *
     * @return name of the person
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for the parents
     *
     * @return list of parents
     */
    public ArrayList<Person> getParents() {
        return parents;
    }

    /**
     * A function to add a new parent to the list of parents
     *
     * @param parent parent to add
     */
    public void addParent(Person parent) {
        this.parents.add(parent);
    }

    /**
     * Getter for the children
     *
     * @return list of children
     */
    public ArrayList<Person> getChildren() {
        return children;
    }

    /**
     * A function to add a new child to the list of children
     *
     * @param child child to add
     */
    public void addChild(Person child) {
        this.children.add(child);
    }

    /**
     * Getter for the spouses
     *
     * @return list of spouses
     */
    public ArrayList<Person> getSpouse() {
        return spouse;
    }

    /**
     * A function to add a new spouse to the list of spouses
     *
     * @param spouse spouse to add
     */
    public void addSpouse(Person spouse) {
        this.spouse.add(spouse);
    }

    /**
     * This function is used to find all matches of a category in case the line matches the given pattern
     *
     * @param pattern         pattern to match
     * @param ln              line of input
     * @param ipname          name of the person
     * @param matches         list of matches
     * @param matchStrings    list of unconfirmed matches
     * @param finalCollection collection where to add the confirmed matches
     */
    public static void closestMatches(Pattern pattern, String ln, String ipname, ArrayList<Person> matches, ArrayList<String> matchStrings, ArrayList<Person> finalCollection) {
        // check the matches
        Matcher mm = pattern.matcher(ln);
        while (mm.find()) {
            matchStrings.add(ln);

            // find the closest match in the string before the match
            String start = ln.substring(0, mm.start());
            Matcher ms = Patterns.lastEntity.matcher(start);
            if (ms.find()) {
                String entity = ms.group();
                int pipePos = entity.indexOf("|");
                int endPos = entity.indexOf("]]");
                if (pipePos < 0 && endPos < 0) {
                    // no pipe and no end, so the whole string is the name
                    Matcher em = Patterns.namedEntity.matcher(entity);
                    // only save it if the distance is small enough
                    if (em.find() && Math.abs(-entity.length() + em.end()) < MAX_DISTANCE) {
                        entity = em.group();
                        Person p = new Person(entity);
                        if (!entity.equals(ipname) && !finalCollection.contains(p)) {
                            finalCollection.add(new Person(entity));
                        }
                    }
                } else {
                    // there is a pipe or end, so the name is between them
                    entity = entity.substring(2);
                    if (entity.startsWith(":simple:")) {
                        entity = entity.substring(8);
                    }
                    pipePos = entity.indexOf("|");
                    endPos = entity.indexOf("]]");
                    if (endPos < 0) {
                        endPos = pipePos;
                    } else if (pipePos != -1) {
                        endPos = Math.min(pipePos, endPos);
                    }
                    // only save it if the distance is small enough
                    if (Math.abs(-start.length() + start.lastIndexOf("]]")) < MAX_DISTANCE) {
                        try {
                            entity = entity.substring(0, endPos);
                            Person p = new Person(entity);
                            if (!matches.contains(p)) {
                                matches.add(p);
                            }
                        } catch (StringIndexOutOfBoundsException ignored) {
                        }
                    }
                }
            }

            // find the closest match in the string after the match
            String end = ln.substring(mm.end());
            if (!(pattern == Patterns.child && end.startsWith("of"))) {
                Matcher me = Patterns.firstEntity.matcher(end);
                if (me.find()) {
                    String entity = me.group();
                    int pipePos = entity.indexOf("|");
                    int endPos = entity.indexOf("]]");
                    if (pipePos < 0 && endPos < 0) {
                        // no pipe and no end, so the whole string is the name
                        Matcher em = Patterns.namedEntity.matcher(entity);
                        // only save it if the distance is small enough
                        if (em.find() && Math.abs(em.start()) < MAX_DISTANCE) {
                            entity = em.group();
                            Person p = new Person(entity);
                            if (!entity.equals(ipname) && !finalCollection.contains(p)) {
                                finalCollection.add(p);
                            }
                        }
                    } else {
                        // there is a pipe or end, so the name is between them
                        entity = entity.substring(entity.indexOf("[[") + 2);
                        if (entity.startsWith(":simple:")) {
                            entity = entity.substring(8);
                        }
                        pipePos = entity.indexOf("|");
                        endPos = entity.indexOf("]]");
                        if (endPos < 0) {
                            endPos = pipePos;
                        } else if (pipePos >= 0) {
                            endPos = Math.min(pipePos, endPos);
                        }
                        // only save it if the distance is small enough
                        if (Math.abs(end.indexOf("[[")) < MAX_DISTANCE) {
                            try {
                                entity = entity.substring(0, endPos);
                                Person p = new Person(entity);
                                if (!matches.contains(p)) {
                                    matches.add(p);
                                }
                            } catch (StringIndexOutOfBoundsException ignored) {
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Parse one page of Wikipedia
     *
     * @param page page to parse
     * @return the person which was parsed or null if no the page is not about a person
     * @throws IOException if the page could not be read
     */
    public static IdentifiedPerson parse(Page page) throws IOException {
        BufferedReader br = new BufferedReader(new StringReader(page.getContent()));
        String ln = "";
        IdentifiedPerson ip = new IdentifiedPerson(page.getTitle());

        // find closest named entities to all matches
        while ((ln = br.readLine()) != null) {
            closestMatches(Patterns.parent, ln, ip.getName(), ip.mh.parentMatches, ip.mh.parentMatchStrings, ip.parents);
            closestMatches(Patterns.child, ln, ip.getName(), ip.mh.childMatches, ip.mh.childMatchStrings, ip.children);
            closestMatches(Patterns.spouse, ln, ip.getName(), ip.mh.spouseMatches, ip.mh.spouseMatchStrings, ip.spouse);
        }

        return ip;
    }

    /**
     * Check that all the identified relatives are people and search for the appropriate IdentifiedPerson in the index
     *
     * @param pi person to check
     */
    public void runBackCheck(PersonIndex pi) {
        if (mh != null) {
            // check all parents
            for (Person p : mh.parentMatches) {
                if (p.getName().equals(this.getName())) {
                    continue;
                }
                Person c = pi.isPerson(p.getName());
                if (c != null && !parents.contains(c)) {
                    parents.add(c);
                }
            }

            // check all children
            for (Person p : mh.childMatches) {
                if (p.getName().equals(this.getName())) {
                    continue;
                }
                Person c = pi.isPerson(p.getName());
                if (c != null && !children.contains(c)) {
                    children.add(c);
                }
            }

            // check all spouses
            for (Person p : mh.spouseMatches) {
                if (p.getName().equals(this.getName())) {
                    continue;
                }
                Person c = pi.isPerson(p.getName());
                if (c != null && !spouse.contains(c)) {
                    spouse.add(c);
                }
            }
            mh = null;
        }
    }

    /**
     * Convert the person to a string for debugging output
     *
     * @return the string
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(name).append("\n");
        if (parents.size() > 0) {
            parents.forEach(p -> sb.append("Parent: ").append(p.getName()).append("\n"));
        } else {
            sb.append("No parents found\n");
        }
        if (children.size() > 0) {
            children.forEach(p -> sb.append("Child: ").append(p.getName()).append("\n"));
        } else {
            sb.append("No children found\n");
        }
        if (spouse.size() > 0) {
            spouse.forEach(p -> sb.append("Spouse: ").append(p.getName()).append("\n"));
        } else {
            sb.append("No spouse found\n");
        }
        return sb.toString();
    }

    /**
     * Get a collection of all relatives of the person
     *
     * @return the collection
     */
    public ArrayList<IdentifiedPerson> getRelatives() {
        ArrayList<IdentifiedPerson> relatives = new ArrayList<>();
        // insert all parents
        for (Person p : parents) {
            if (p instanceof IdentifiedPerson) {
                relatives.add((IdentifiedPerson) p);
            }
        }
        // insert all children
        for (Person p : children) {
            if (p instanceof IdentifiedPerson) {
                relatives.add((IdentifiedPerson) p);
            }
        }
        // insert all spouses
        for (Person p : spouse) {
            if (p instanceof IdentifiedPerson) {
                relatives.add((IdentifiedPerson) p);
            }
        }
        return relatives;
    }
}
