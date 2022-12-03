package sk.vighnorbert;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;

public class Main {

    public static String FILENAME = "/home/ubuntu/Projects/wiki/articles.xml";
    public static boolean DEBUG = false;

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);
        String in = "";

        while (true) {
            System.out.println("Available commands:");
            System.out.println(" parse  - parse the input wiki file for relevant results");
            System.out.println(" search - search in existing results");
            System.out.println(" exit   - quit application\n");

            System.out.println("Enter a command which should be executed:");
            in = scanner.nextLine();

            if (in.equals("exit")) {
                System.out.println("Exiting...");
                break;
            }
            switch (in) {
                case "parse":
                    System.out.println("Parsing...");
                    parse();
                    break;
                case "search":
                    System.out.println("Searching...");
                    search();
                    break;
                default:
                    System.out.println("Unknown command.\n");
                    break;
            }
        }
    }

    public static void parse() throws IOException {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));

        SparkSession spark = SparkSession.builder()
                .master("local[*]").appName("FamilyTree")
                .config("spark.driver.maxResultSize", "4g").getOrCreate();

        StructType schema = new StructType()
                .add("id", "long")
                .add("ns", "long")
                .add("redirect", new StructType()
                        .add("_VALUE", "string")
                        .add("_title", "string"))
                .add("revision", new StructType()
                        .add("text", new StructType()
                                .add("_VALUE", "string")
                                .add("_bytes", "long")
                                .add("_xml:space", "string")))
                .add("title", "string");


        JavaRDD<Row> pagesDf = spark.sqlContext().read()
                .format("com.databricks.spark.xml")
                .option("rowTag", "page").schema(schema)
                .load(FILENAME).toJavaRDD();

        JavaRDD<IdentifiedPerson> jrddip = pagesDf.map(page -> {

            Page p = Page.read(page);
            if (p != null) {
                return IdentifiedPerson.parse(p);
            }
            return null;
        }).filter(Objects::nonNull);

        PersonIndex index = new PersonIndex();

        for (IdentifiedPerson person : jrddip.collect()) {
            index.addPerson(person);
        }
        index.out();
        index.runBackCheck();
        index.writeToFile();

        System.out.println("Parsing done.");
    }
    public static void search() throws IOException {
        PersonIndex index = new PersonIndex();

        index.readFromFile();

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
                        if (i == 10) {
                            System.out.println("More than 10 results found, showing only first 10. Write \"more\" to show all.");
                            break;
                        }
                    }
                    System.out.println("Enter index of person to see details:");
                }
                while (true) {
                    String si = (people.size() == 1) ? "1" : scanner.nextLine();
                    if (si == "more") {
                        for (Person person : people) {
                            i++;
                            System.out.println("[" + i + "] " + person.getName());
                        }
                        System.out.println("Enter index of person to see details:");
                        si = (people.size() == 1) ? "1" : scanner.nextLine();
                    }
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
            }
        }
    }

}