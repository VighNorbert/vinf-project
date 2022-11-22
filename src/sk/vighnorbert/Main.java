package sk.vighnorbert;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

//import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

public class Main {

    public static String FILENAME = "/home/ubuntu/Projects/wiki/articles1.xml";
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

        SparkConf sparkConf = new SparkConf().setMaster("local[*]").setAppName("FamilyTree");
        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        JavaRDD<String> inputFile = sc.textFile(FILENAME);

        SQLContext sqlc = new SQLContext(sc);

        // za .option sa da pridat .schema() a tam trochu carovat
        JavaRDD<Row> xmlDf = sqlc.read().format("com.databricks.spark.xml").option("rowTag", "page").load(FILENAME).toJavaRDD();

        xmlDf.foreach(row -> {
            System.out.println("row");
            System.out.println(row);
        });

        /*
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
        */
    }

}