package sk.vighnorbert;

import java.util.regex.Pattern;

public class Patterns {

    public static final Pattern pageStart = Pattern.compile("<page>");
    public static final Pattern pageEnd = Pattern.compile("</page>");
    public static final Pattern title = Pattern.compile("<title>.*</title>");
    public static final Pattern titleStart = Pattern.compile("<title>");
    public static final Pattern titleEnd = Pattern.compile("</title>");
    public static final Pattern categoryPeople = Pattern.compile("\\[\\[Category:People ");
    public static final Pattern parent = Pattern.compile("\\W([Hh]is |[Hh]er )?([Mm]other|[Ff]ather)[^'\\w]");
    public static final Pattern child = Pattern.compile("\\W([Cc]hild(ren)?|[Dd]aughter|[Ss]on)\\W");
    public static final Pattern spouse = Pattern.compile("\\Wmarried|spouse|wife|husband\\W");

    public static final Pattern firstEntity = Pattern.compile("^[^a-zA-Z0-9_.]*(\\w+[^a-zA-Z0-9_.=]+){0,10}?\\[\\[(:simple:)?[A-Z].*?(]]|\\|)|^[^a-zA-Z0-9_.]*(\\w+[^a-zA-Z0-9_.=]+){0,3}?([A-Z][a-z-]+\\s*){2,}");
    public static final Pattern lastEntity = Pattern.compile("\\[\\[(:simple:)?[A-Z][^]]*?]]([^a-zA-Z0-9_.]+\\w+){0,10}?[^a-zA-Z0-9_.]*$|[^.]\\s+([A-Z][a-z-]+\\s+){2,}(\\w+[^a-zA-Z0-9_.]+){0,3}?[^a-zA-Z0-9_.]*$");
    public static final Pattern namedEntity = Pattern.compile("([A-Z][a-z-]+\\s*)*([A-Z][a-z-]+)");

}
