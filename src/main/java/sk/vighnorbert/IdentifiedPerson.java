package sk.vighnorbert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdentifiedPerson extends Person {
    private String birthDate;
    private String deathDate;
    private String birthPlace;
    private String deathPlace;

    private final ArrayList<Person> parents;
    private final ArrayList<Person> children;
    private final ArrayList<Person> spouse;


    private IdentifiedPerson(String name) {
        super(name);
        this.parents = new ArrayList<>();
        this.children = new ArrayList<>();
        this.spouse = new ArrayList<>();
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

    public static void addMatch(HashMap<String, ArrayList<Integer>> matchMap, String entity, int distance) {
        if (matchMap.containsKey(entity)) {
            matchMap.get(entity).add(distance);
        } else {
            ArrayList<Integer> list = new ArrayList<>();
            list.add(distance);
            matchMap.put(entity, list);
        }
    }


    public static void closestMatches(Pattern pattern, String ln, HashMap<String, ArrayList<Integer>> matchMap, ArrayList<String> matchStrings) {
        Matcher mm = pattern.matcher(ln);
        if (mm.find()) {
            matchStrings.add(ln);

            String start = ln.substring(0, mm.start());
            Matcher ms = Patterns.lastEntity.matcher(start);
            if (ms.find()) {
                String entity = ms.group();
                int pipePos = entity.indexOf("|");
                int endPos = entity.indexOf("]]");
                if (pipePos < 0 && endPos < 0) {
                    Matcher em = Patterns.namedEntity.matcher(entity);
                    if (em.find()) {
                        entity = em.group();
                        addMatch(matchMap, entity, -entity.length() + em.end());
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
                    try {
                        entity = entity.substring(0, endPos);
                        addMatch(matchMap, entity, -start.length() + start.lastIndexOf("]]"));
                    } catch (StringIndexOutOfBoundsException ignored) {}
                }
            }

            String end = ln.substring(mm.end());
            if (pattern == Patterns.child && end.startsWith("of")) {
                return;
            }
            Matcher me = Patterns.firstEntity.matcher(end);
            if (me.find()) {
                String entity = me.group();
                int pipePos = entity.indexOf("|");
                int endPos = entity.indexOf("]]");
                if (pipePos < 0 && endPos < 0) {
                    Matcher em = Patterns.namedEntity.matcher(entity);
                    if (em.find()) {
                        entity = em.group();
                        addMatch(matchMap, entity, em.start());
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
                    try {
                        entity = entity.substring(0, endPos);
                        addMatch(matchMap, entity, end.indexOf("[["));
                    } catch (StringIndexOutOfBoundsException ignored) {}
                }
            }
        }
    }

    public static IdentifiedPerson parse(Page page) throws IOException {
        BufferedReader br = new BufferedReader(new StringReader(page.getContent()));
        String ln = "";
        IdentifiedPerson person = new IdentifiedPerson(page.getTitle());

        ArrayList<String> parentMatchStrings = new ArrayList<>();
        ArrayList<String> childMatchStrings = new ArrayList<>();
        ArrayList<String> spouseMatchStrings = new ArrayList<>();
        HashMap<String, ArrayList<Integer>> parentMatches = new HashMap<>();
        HashMap<String, ArrayList<Integer>> childMatches = new HashMap<>();
        HashMap<String, ArrayList<Integer>> spouseMatches = new HashMap<>();

        while ((ln = br.readLine()) != null) {
            closestMatches(Patterns.parent, ln, parentMatches, parentMatchStrings);
            closestMatches(Patterns.child, ln, childMatches, childMatchStrings);
            closestMatches(Patterns.spouse, ln, spouseMatches, spouseMatchStrings);
        }

        if (Main.DEBUG) {
            System.out.println("\nParent matches: " + parentMatchStrings.size());
            for (String match : parentMatchStrings) {
                System.out.println(match);
            }
            System.out.println("Relevant Parent matches: " + parentMatches.size());
            for (Map.Entry<String, ArrayList<Integer>> entry : parentMatches.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }

            System.out.println("\nChild matches: " + childMatchStrings.size());
            for (String match : childMatchStrings) {
                System.out.println(match);
            }
            System.out.println("Relevant Child matches: " + childMatches.size());
            for (Map.Entry<String, ArrayList<Integer>> entry : childMatches.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }

            System.out.println("\nSpouse matches: " + spouseMatchStrings.size());
            for (String match : spouseMatchStrings) {
                System.out.println(match);
            }
            System.out.println("Relevant Spouse matches: " + spouseMatches.size());
            for (Map.Entry<String, ArrayList<Integer>> entry : spouseMatches.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }

            // vyhodit objekty ktore nie su ludia (vytvorime si zoznam
        }

        return person;
    }
}
