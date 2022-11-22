package sk.vighnorbert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdentifiedPerson extends Person {

    private final static int MAX_DISTANCE = 10;
    private String birthDate;
    private String birthPlace;

    private String deathDate;
    private String deathPlace;

    private MatchesHolder mh;

    private final ArrayList<Person> parents;
    private final ArrayList<Person> children;
    private final ArrayList<Person> spouse;


    private IdentifiedPerson(String name) {
        super(name);
        this.parents = new ArrayList<>();
        this.children = new ArrayList<>();
        this.spouse = new ArrayList<>();
        this.mh = new MatchesHolder();
    }

    public String getName() {
        return name;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(String deathDate) {
        this.deathDate = deathDate;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    public String getDeathPlace() {
        return deathPlace;
    }

    public void setDeathPlace(String deathPlace) {
        this.deathPlace = deathPlace;
    }

    public ArrayList<Person> getParents() {
        return parents;
    }

    public void addParent(Person parent) {
        this.parents.add(parent);
    }

    public ArrayList<Person> getChildren() {
        return children;
    }

    public void addChild(Person child) {
        this.children.add(child);
    }

    public ArrayList<Person> getSpouse() {
        return spouse;
    }

    public void addSpouse(Person spouse) {
        this.spouse.add(spouse);
    }

    public static void closestMatches(Pattern pattern, String ln, String ipname, ArrayList<Person> matches, ArrayList<String> matchStrings, ArrayList<Person> finalCollection) {
        Matcher mm = pattern.matcher(ln);
        while (mm.find()) {
            if (Main.DEBUG && pattern == Patterns.parent) {
                System.out.println("tu som");
                System.out.println(ln);
            }
            matchStrings.add(ln);

            String start = ln.substring(0, mm.start());
            Matcher ms = Patterns.lastEntity.matcher(start);
            if (ms.find()) {
                String entity = ms.group();
                int pipePos = entity.indexOf("|");
                int endPos = entity.indexOf("]]");
                if (pipePos < 0 && endPos < 0) {
                    Matcher em = Patterns.namedEntity.matcher(entity);
                    if (em.find() && Math.abs(-entity.length() + em.end()) < MAX_DISTANCE) {
                        entity = em.group();
                        Person p = new Person(entity);
                        if (!entity.equals(ipname) && !finalCollection.contains(p)) {
                            finalCollection.add(new Person(entity));
                        }
                    }
                } else {
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

            String end = ln.substring(mm.end());
            if (Main.DEBUG && pattern == Patterns.parent) {
                System.out.println("end: " + end);
            }
            if (!(pattern == Patterns.child && end.startsWith("of"))) {
                Matcher me = Patterns.firstEntity.matcher(end);
                if (me.find()) {
                    String entity = me.group();
                    int pipePos = entity.indexOf("|");
                    int endPos = entity.indexOf("]]");
                    if (pipePos < 0 && endPos < 0) {
                        Matcher em = Patterns.namedEntity.matcher(entity);
                        if (em.find() && Math.abs(em.start()) < MAX_DISTANCE) {
                            entity = em.group();
                            Person p = new Person(entity);
                            if (!entity.equals(ipname) && !finalCollection.contains(p)) {
                                finalCollection.add(p);
                            }
                        }
                    } else {
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

    public static IdentifiedPerson parse(Page page) throws IOException {
        BufferedReader br = new BufferedReader(new StringReader(page.getContent()));
        String ln = "";
        IdentifiedPerson ip = new IdentifiedPerson(page.getTitle());

        while ((ln = br.readLine()) != null) {
            closestMatches(Patterns.parent, ln, ip.getName(), ip.mh.parentMatches, ip.mh.parentMatchStrings, ip.parents);
            closestMatches(Patterns.child, ln, ip.getName(), ip.mh.childMatches, ip.mh.childMatchStrings, ip.children);
            closestMatches(Patterns.spouse, ln, ip.getName(), ip.mh.spouseMatches, ip.mh.spouseMatchStrings, ip.spouse);
        }

        if (Main.DEBUG) {
            System.out.println("\nParent matches: " + ip.mh.parentMatchStrings.size());
            for (String match : ip.mh.parentMatchStrings) {
                System.out.println(match);
            }
            System.out.println("Relevant Parent matches: " + ip.mh.parentMatches.size());
            for (Person person : ip.mh.parentMatches) {
                System.out.println(person.getName());
            }
            System.out.println("Final Parent matches: " + ip.parents.size());
            for (Person person : ip.parents) {
                System.out.println(person.getName());
            }

            System.out.println("\nChild matches: " + ip.mh.childMatchStrings.size());
            for (String match : ip.mh.childMatchStrings) {
                System.out.println(match);
            }
            System.out.println("Relevant Child matches: " + ip.mh.childMatches.size());
            for (Person person : ip.mh.childMatches) {
                System.out.println(person.getName());
            }
            System.out.println("Final Child matches: " + ip.children.size());
            for (Person person : ip.children) {
                System.out.println(person.getName());
            }

            System.out.println("\nSpouse matches: " + ip.mh.spouseMatchStrings.size());
            for (String match : ip.mh.spouseMatchStrings) {
                System.out.println(match);
            }
            System.out.println("Relevant Spouse matches: " + ip.mh.spouseMatches.size());
            for (Person person : ip.mh.spouseMatches) {
                System.out.println(person.getName());
            }
            System.out.println("Final Spouse matches: " + ip.spouse.size());
            for (Person person : ip.spouse) {
                System.out.println(person.getName());
            }
        }

        return ip;
    }

    public void runBackCheck(PersonIndex pi) {
        if (mh != null) {
            for (Person p : mh.parentMatches) {
                if (p.getName().equals(this.getName())) {
                    continue;
                }
                Person c = pi.isPerson(p.getName());
                if (c != null && !parents.contains(c)) {
                    parents.add(c);
                }
            }
            for (Person p : mh.childMatches) {
                if (p.getName().equals(this.getName())) {
                    continue;
                }
                Person c = pi.isPerson(p.getName());
                if (c != null && !children.contains(c)) {
                    children.add(c);
                }
            }
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
}
