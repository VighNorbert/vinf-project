package sk.vighnorbert;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;

public class Page {
    private final String title;

    private String content;

    public Page(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

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

    public static Page read(Row row) throws IOException {
        Page p = new Page(row.getAs("title").toString());
        StringBuilder content = new StringBuilder();
        boolean isPerson = false;

        GenericRowWithSchema revision = row.getAs("revision");
        GenericRowWithSchema text = revision.getAs("text");
        if (text == null) {
            return null;
        }
        String pageText = text.getAs("_VALUE");
        if (pageText == null) {
            return null;
        }
        BufferedReader reader = new BufferedReader(new StringReader(pageText));
        // while not end of page
        String ln = "";
        while ((ln = reader.readLine()) != null) {
            if (!isPerson && Patterns.categoryPeople.matcher(ln).find()) {
                isPerson = true;
            }
            ln = ln.replaceAll("\\s+", " ");
            content.append(ln).append(System.getProperty("line.separator"));
        }
        content.append(ln);
        if (isPerson) {
            p.setContent(content.toString());
            return p;
        }
        return null;
    }
}
