package sk.vighnorbert;

import java.io.*;
import java.nio.charset.StandardCharsets;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

public class Main {

//    public static String FILENAME = "C:\\wiki\\enwiki-20221020-pages-articles-multistream.xml.bz2";
    public static String FILENAME = "C:\\wiki\\enwiki-latest-pages-articles1.xml";
//    public static String FILENAME = "C:\\wiki\\enwiki-pages-articles.xml";
    public static boolean DEBUG = false;
    public static boolean VERBOSE = false;

    public static void main(String[] args) throws IOException {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));

        File file = new File(FILENAME);

        // read from xml
        BufferedReader reader = new BufferedReader(new FileReader(file));

        // read from bz2
//        InputStream inputStream = new BZip2CompressorInputStream(new FileInputStream(file), true);
//        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        PersonIndex index = new PersonIndex();

        String ln = "";
        int pagesCount = 0, personCount = 0;

        while ((ln = reader.readLine()) != null) {
            if (Patterns.pageStart.matcher(ln).find()) {
                pagesCount++;
                Page page = Page.read(reader, ln);
                if (page != null) {
                    personCount++;

                    DEBUG = personCount == 1;

                    if (DEBUG) {
                        System.out.println(page.getTitle());
                        System.out.println();
                    }

                    IdentifiedPerson person = IdentifiedPerson.parse(page);
                    index.addPerson(person);

                    if (DEBUG && VERBOSE) {
                        System.out.println("\nFull page contents:");
                        System.out.print(page.getContent());
                        break;
                    }
                }
                if (pagesCount % 1000 == 0) {
                    System.out.println(personCount + " / " + pagesCount + "...");
                }
            }
        }

        index.runBackCheck();

        IdentifiedPerson ip = (IdentifiedPerson) index.isPerson("Abraham Lincoln");
        System.out.println("Abraham Lincoln");
        ip.getParents().forEach((p) -> {
            System.out.println("parent: \"" + p.getName() + "\"");
        });
        ip.getChildren().forEach((p) -> {
            System.out.println("children: \"" + p.getName() + "\"");
        });
        ip.getSpouse().forEach((p) -> {
            System.out.println("spouse: \"" + p.getName() + "\"");
        });

        System.out.println(pagesCount + " total articles");
        System.out.println(personCount + " people found");

    }

}