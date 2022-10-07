package sk.vighnorbert;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static final Pattern pPageStart = Pattern.compile("<page>");
    private static final Pattern pPageEnd = Pattern.compile("</page>");
    private static final Pattern pTitle = Pattern.compile("<title>.*</title>");
    private static final Pattern pTitleStart = Pattern.compile("<title>");
    private static final Pattern pTitleEnd = Pattern.compile("</title>");
    private static final Pattern pCategoryPeople = Pattern.compile("\\[\\[Category:People");

    private static int personCount = 0;

    public static void main(String[] args) throws IOException {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));

        File file = new File("C:\\wiki\\enwiki-latest-pages-articles1.xml");

        BufferedReader reader = new BufferedReader(new FileReader(file));


        String ln = "";
        int i = 0;
        while ((ln = reader.readLine()) != null) {
            if (pPageStart.matcher(ln).find()) {
                i++;
                readPage(reader);
            }
        }

        System.out.println(i);
        System.out.println(personCount + " people found");

    }

    private static String readPage(BufferedReader reader) throws IOException {
        String ln = "";
        String title = null;
        StringBuilder content = new StringBuilder();
        boolean isPerson = false;
        while ((ln = reader.readLine()) != null && !pPageEnd.matcher(ln).find()) {
            if (title == null && pTitle.matcher(ln).find()) {
                Matcher ms = pTitleStart.matcher(ln);
                Matcher me = pTitleEnd.matcher(ln);
                ms.find(); me.find();
                title = ln.substring(ms.end(), me.start());
            }
            if (!isPerson && pCategoryPeople.matcher(ln).find()) {
                System.out.println(title + " may be a person.");
                isPerson = true;
                personCount++;
            }
            content.append(ln);
        }
        return title;
    }
}