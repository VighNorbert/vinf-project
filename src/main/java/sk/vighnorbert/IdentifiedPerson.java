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

    private Person mother;
    private Person father;
    private final ArrayList<Person> children;


    private IdentifiedPerson(String name) {
        super(name);
        this.children = new ArrayList<>();

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

    public Person getMother() {
        return mother;
    }

    public void setMother(Person mother) {
        this.mother = mother;
    }

    public Person getFather() {
        return father;
    }

    public void setFather(Person father) {
        this.father = father;
    }

    public ArrayList<Person> getChildren() {
        return children;
    }

    public void addChild(Person child) {
        this.children.add(child);
    }


    public static void closestMatches(Pattern pattern, String ln, HashMap<String, ArrayList<Integer>> matchMap, ArrayList<String> matchStrings) {
        Matcher mm = pattern.matcher(ln);
        if (mm.find()) {
            matchStrings.add(ln);

            String start = ln.substring(0, mm.start());
            try {
                int endPos = start.lastIndexOf("]]");
                String ce = start.substring(start.lastIndexOf("[[") + 2, start.lastIndexOf("]]"));
                if (ce.contains("|")) {
                    ce = ce.substring(0, ce.indexOf("|"));
                }
                if (matchMap.containsKey(ce)) {
                    matchMap.get(ce).add(start.length() - endPos);
                } else {
                    ArrayList<Integer> distances = new ArrayList<>();
                    distances.add(start.length() - endPos);
                    matchMap.put(ce, distances);
                }
            } catch (StringIndexOutOfBoundsException ignored) {
//                System.out.println("Error parsing: " + start);
//                System.out.println(start.lastIndexOf("[[") + ":" + start.lastIndexOf("]]"));
            }

            String end = ln.substring(mm.end());
            try {
                int startPos = end.indexOf("[[");
                String ce = end.substring(startPos + 2, end.indexOf("]]"));
                if (ce.contains("|")) {
                    ce = ce.substring(0, ce.indexOf("|"));
                }
                if (matchMap.containsKey(ce)) {
                    matchMap.get(ce).add(startPos);
                } else {
                    ArrayList<Integer> distances = new ArrayList<>();
                    distances.add(startPos);
                    matchMap.put(ce, distances);
                }
            } catch (StringIndexOutOfBoundsException ignored) {
//                System.out.println("Error parsing: " + end);
//                System.out.println(end.indexOf("[[") + ":min(" + end.indexOf("|") + "," + end.indexOf("]]") + ")");
            }
        }
    }

    public static IdentifiedPerson parse(Page page) throws IOException {
        BufferedReader br = new BufferedReader(new StringReader(page.getContent()));
        String ln = "";
        IdentifiedPerson person = new IdentifiedPerson(page.getTitle());

        ArrayList<String> motherMatchStrings = new ArrayList<>();
        ArrayList<String> fatherMatchStrings = new ArrayList<>();
        ArrayList<String> childMatchStrings = new ArrayList<>();
        HashMap<String, ArrayList<Integer>> motherMatches = new HashMap<>();
        HashMap<String, ArrayList<Integer>> fatherMatches = new HashMap<>();
        HashMap<String, ArrayList<Integer>> childMatches = new HashMap<>();

        while ((ln = br.readLine()) != null) {
            closestMatches(Patterns.mother, ln, motherMatches, motherMatchStrings);
            closestMatches(Patterns.father, ln, fatherMatches, fatherMatchStrings);
            closestMatches(Patterns.child, ln, childMatches, childMatchStrings);
        }

        if (Main.DEBUG) {
            System.out.println("\nMother matches: " + motherMatchStrings.size());
            for (String match : motherMatchStrings) {
                System.out.println(match);
            }
            for (Map.Entry<String, ArrayList<Integer>> entry : motherMatches.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }

            System.out.println("\nFather matches: " + fatherMatchStrings.size());
            for (String match : fatherMatchStrings) {
                System.out.println(match);
            }
            for (Map.Entry<String, ArrayList<Integer>> entry : fatherMatches.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }

            System.out.println("\nChild matches: " + childMatchStrings.size());
            for (String match : childMatchStrings) {
                System.out.println(match);
            }
            for (Map.Entry<String, ArrayList<Integer>> entry : childMatches.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        }

        return person;
    }
}
