package sk.vighnorbert;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Main {

    public static String FILENAME = "C:\\wiki\\enwiki-latest-pages-articles1.xml";
    public static boolean DEBUG = false;

    public static void main(String[] args) throws IOException {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));

        File file = new File(FILENAME);

        BufferedReader reader = new BufferedReader(new FileReader(file));

        String ln = "";
        int pagesCount = 0, personCount = 0;

        while ((ln = reader.readLine()) != null) {
            if (Patterns.pageStart.matcher(ln).find()) {
                pagesCount++;
                Page page = Page.read(reader, ln);
                if (page != null) {
                    personCount++;

//                    DEBUG = personCount == 4;

                    if (DEBUG) {
                        System.out.println(page.getTitle());
                        System.out.println();
                    }

                    IdentifiedPerson person = IdentifiedPerson.parse(page);

                    if (DEBUG) {
                        System.out.println("\nFull page contents:");
                        System.out.print(page.getContent());
                        break;
                    }
                }
            }
        }

        System.out.println(pagesCount + " total articles");
        System.out.println(personCount + " people found");

    }

}