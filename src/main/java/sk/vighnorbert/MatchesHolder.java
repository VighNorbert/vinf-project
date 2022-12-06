package sk.vighnorbert;

import java.io.Serializable;
import java.util.ArrayList;

public class MatchesHolder implements Serializable {

    public final ArrayList<String> parentMatchStrings = new ArrayList<>();
    public final ArrayList<String> childMatchStrings = new ArrayList<>();
    public final ArrayList<String> spouseMatchStrings = new ArrayList<>();
    public final ArrayList<Person> parentMatches = new ArrayList<>();
    public final ArrayList<Person> childMatches = new ArrayList<>();
    public final ArrayList<Person> spouseMatches = new ArrayList<>();

}
