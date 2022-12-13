package sk.vighnorbert;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;

/**
 * Class holding information about one Page from the Wikipedia dump.
 *
 * @author Norbert Vígh
 */
public class Page {

    /**
     * The title of the page
     */
    private final String title;

    /**
     * The text of the page
     */
    private String content;

    public Page(String title) {
        this.title = title;
    }

    /**
     * Getter for the title of the page
     *
     * @return the title of the page
     */
    public String getTitle() {
        return title;
    }

    /**
     * Getter for the content of the page
     *
     * @return the content of the page
     */
    public String getContent() {
        return content;
    }

    /**
     * Setter for the content of the page
     *
     * @param content the content of the page
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Original function used for reading a page from the Wikipedia dump
     *
     * @param reader the reader to read from
     * @param ln     the first line of the page
     * @return the parsed page
     * @throws IOException if an I/O error occurs
     * @deprecated use {@link #read(Row)} instead
     */
    public static Page read(BufferedReader reader, String ln) throws IOException {
        Page p = null;
        StringBuilder content = new StringBuilder();
        boolean isPerson = false;
        // while not end of page
        do {
            if (p == null && Patterns.title.matcher(ln).find()) {
                // try to match and get title
                Matcher ms = Patterns.titleStart.matcher(ln);
                Matcher me = Patterns.titleEnd.matcher(ln);
                if (ms.find() && me.find()) {
                    String title = ln.substring(ms.end(), me.start());
                    p = new Page(title);
                }
            }
            if (!isPerson && Patterns.categoryPeople.matcher(ln).find()) {
                isPerson = true;
            }
            ln = ln.replaceAll("\\s+", " ");
            content.append(ln).append(System.getProperty("line.separator"));
        } while ((ln = reader.readLine()) != null && !Patterns.pageEnd.matcher(ln).find());
        content.append(ln);
        if (isPerson && p != null) {
            p.setContent(content.toString());
            return p;
        }
        return null;
    }

    /**
     * The new function for distributing the parsing of the pages
     *
     * @param row the row to parse
     * @return the parsed page
     * @throws IOException if an I/O error occurs
     */
    public static Page read(Row row) throws IOException {
        Page p = new Page(row.getAs("title").toString());
        StringBuilder content = new StringBuilder();

        GenericRowWithSchema revision = row.getAs("revision");
        GenericRowWithSchema text = revision.getAs("text");
        if (text == null) {
            return null;
        }
        String pageText = text.getAs("_VALUE");
        if (pageText == null) {
            return null;
        }

        // read the contents to check if the page is a person
        BufferedReader reader = new BufferedReader(new StringReader(pageText));
        boolean isPerson = false;
        String ln = "";
        // while not end of page
        while ((ln = reader.readLine()) != null) {
            if (!isPerson && Patterns.categoryPeople.matcher(ln).find()) {
                isPerson = true;
            }
            ln = ln.replaceAll("\\s+", " ");
            content.append(ln).append(System.getProperty("line.separator"));
        }
        content.append(ln);
        if (isPerson) {
            // only add the page if it is a person
            p.setContent(content.toString());
            return p;
        }
        return null;
    }
}
