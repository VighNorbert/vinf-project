package sk.vighnorbert;

import java.util.regex.Pattern;

public class Patterns {

    public static final Pattern pageStart = Pattern.compile("<page>");
    public static final Pattern pageEnd = Pattern.compile("</page>");
    public static final Pattern title = Pattern.compile("<title>.*</title>");
    public static final Pattern titleStart = Pattern.compile("<title>");
    public static final Pattern titleEnd = Pattern.compile("</title>");
    public static final Pattern categoryPeople = Pattern.compile("\\[\\[Category:People");
    public static final Pattern mother = Pattern.compile("\\bmother\\b");
    public static final Pattern father = Pattern.compile("\\bfather\\b");
    public static final Pattern child = Pattern.compile("\\W(child(ren)?|daughter|son)\\W");

}
