package sk.vighnorbert;

import java.util.regex.Pattern;

/**
 * Class holding all the regular expressions used in the project.
 *
 * @author Norbert Vígh
 */
public class Patterns {

    /**
     * Pattern for matching the end of a page
     */
    public static final Pattern pageEnd = Pattern.compile("</page>");

    /**
     * Pattern for matching the title of a page
     */
    public static final Pattern title = Pattern.compile("<title>.*</title>");

    /**
     * Pattern for matching the start of a title of the page
     */
    public static final Pattern titleStart = Pattern.compile("<title>");

    /**
     * Pattern for matching the end of a title of the page
     */
    public static final Pattern titleEnd = Pattern.compile("</title>");

    /**
     * Pattern for matching the category of some people
     */
    public static final Pattern categoryPeople = Pattern.compile("\\[\\[Category:People ");

    /**
     * Pattern for matching a line containing information about a parent
     */
    public static final Pattern parent = Pattern.compile("\\W([Hh]is |[Hh]er )?([Mm]other|[Ff]ather)[^'\\w]");

    /**
     * Pattern for matching a line containing information about a child
     */
    public static final Pattern child = Pattern.compile("\\W([Cc]hild(ren)?|[Dd]aughter|[Ss]on)\\W");

    /**
     * Pattern for matching a line containing information about a spouse
     */
    public static final Pattern spouse = Pattern.compile("\\Wmarried|spouse|wife|husband\\W");

    /**
     * Pattern for finding the first named entity in a line
     */
    public static final Pattern firstEntity = Pattern.compile("^[^a-zA-Z0-9_.]*(\\w+[^a-zA-Z0-9_.=]+){0,10}?\\[\\[(:simple:)?[A-Z].*?(]]|\\|)|^[^a-zA-Z0-9_.]*(\\w+[^a-zA-Z0-9_.=]+){0,3}?([A-Z][a-z-]+\\s*){2,}");

    /**
     * Pattern for finding the last named entity in a line
     */
    public static final Pattern lastEntity = Pattern.compile("\\[\\[(:simple:)?[A-Z][^]]*?]]([^a-zA-Z0-9_.]+\\w+){0,10}?[^a-zA-Z0-9_.]*$|[^.]\\s+([A-Z][a-z-]+\\s+){2,}(\\w+[^a-zA-Z0-9_.]+){0,3}?[^a-zA-Z0-9_.]*$");

    /**
     * Pattern for finding any named entity
     */
    public static final Pattern namedEntity = Pattern.compile("([A-Z][a-z-]+\\s*)*([A-Z][a-z-]+)");

}
