package sk.vighnorbert;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class holding all the potential (not identified) matches
 *
 * @author Norbert Vígh
 */
public class MatchesHolder implements Serializable {

    /**
     * Collection of all potential parents in string form for debugging purposes
     */
    public final ArrayList<String> parentMatchStrings = new ArrayList<>();

    /**
     * Collection of all potential children in string form for debugging purposes
     */
    public final ArrayList<String> childMatchStrings = new ArrayList<>();

    /**
     * Collection of all potential spouses in string form for debugging purposes
     */
    public final ArrayList<String> spouseMatchStrings = new ArrayList<>();

    /**
     * Collection of all potential parents
     */
    public final ArrayList<Person> parentMatches = new ArrayList<>();

    /**
     * Collection of all potential children
     */
    public final ArrayList<Person> childMatches = new ArrayList<>();

    /**
     * Collection of all potential spouses
     */
    public final ArrayList<Person> spouseMatches = new ArrayList<>();

}
