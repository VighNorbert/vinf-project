package sk.vighnorbert;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

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

//                    DEBUG = page.getTitle().equals("Isaac Newton");

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

        System.out.println(pagesCount + " total articles");
        System.out.println(personCount + " people found");

        Scanner scanner = new Scanner(System.in);

        String input = "";

        while (true) {
            System.out.println("Enter name to search for (or \"exit\" to end):");
            input = scanner.nextLine();

            if (input.equals("exit")) {
                System.out.println("Exiting...");
                break;
            }

            ArrayList<Person> people = index.getResults(input);
            if (people.size() == 0) {
                System.out.println("No person found with name \"" + input + "\"");
            } else {
                System.out.println("Found " + people.size() + " people with name \"" + input + "\":");
                int i = 0;
                if (people.size() > 1) {
                    for (Person person : people) {
                        i++;
                        System.out.println("[" + i + "] " + person.getName());
                    }
                    System.out.println("Enter index of person to see details:");
                }
                while (true) {
                    String si = (people.size() == 1) ? "1" : scanner.nextLine();
                    try {
                        i = Integer.parseInt(si);
                        if (i > 0 && i <= people.size()) {
                            IdentifiedPerson ip = (IdentifiedPerson) people.get(i - 1);
                            System.out.println(ip.toString());
                            break;
                        } else {
                            System.out.println("Invalid index, try again:");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid index, try again:");
                    }
                }
//
            }
        }

    }

}