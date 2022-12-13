package sk.vighnorbert;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;

/**
 * The main class of the program.
 *
 * @author Norbert Vígh
 */
public class Main {

    /**
     * filename of the dump file from wikipedia
     */
    public static String FILENAME = "C:\\wiki\\enwiki-pages-articles.xml";

    /**
     * Main function of the program
     *
     * @param args command line arguments
     * @throws IOException if the file is not found
     */
    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);
        String in = "";

        // infinite loop for the program which can end when the user types "exit"
        while (true) {
            System.out.println("Available commands:");
            System.out.println(" parse  - parse the input wiki file for relevant results");
            System.out.println(" search - search in existing results");
            System.out.println(" relatives - check if two people are related");
            System.out.println(" exit   - quit application\n");

            // wait for user input
            System.out.println("Enter a command which should be executed:");
            in = scanner.nextLine();

            // run the appropriate function based on the user input
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
                case "relatives":
                    System.out.println("Checking for relatives...");
                    relatives(null, null, null);
                    break;
                default:
                    System.out.println("Unknown command.\n");
                    break;
            }
        }
    }

    /**
     * Function for parsing the Wikipedia input file
     *
     * @throws IOException if the file is not found
     */
    public static void parse() throws IOException {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

        // initial setup
        SparkSession spark = SparkSession.builder().master("local[*]").appName("FamilyTree").config("spark.driver.maxResultSize", "4g").getOrCreate();

        // defining the structure of the file
        StructType schema = new StructType().add("id", "long").add("ns", "long").add("redirect", new StructType().add("_VALUE", "string").add("_title", "string")).add("revision", new StructType().add("text", new StructType().add("_VALUE", "string").add("_bytes", "long").add("_xml:space", "string"))).add("title", "string");


        // generate a set of wiki pages from the input file
        JavaRDD<Row> pagesDf = spark.sqlContext().read().format("com.databricks.spark.xml").option("rowTag", "page").schema(schema).load(FILENAME).toJavaRDD();

        // parse the wiki pages and generate a set of people
        JavaRDD<IdentifiedPerson> jrddip = pagesDf.map(page -> {
            Page p = Page.read(page);
            if (p != null) {
                return IdentifiedPerson.parse(p);
            }
            return null;
        }).filter(Objects::nonNull);

        // create an index of people and add all people to it
        PersonIndex index = new PersonIndex();

        for (IdentifiedPerson person : jrddip.collect()) {
            index.addPerson(person);
        }
        index.out();

        // check if all the found matches are really people and serialize the data to a file
        index.runBackCheck();
        index.writeToFile();

        System.out.println("Parsing done.");
    }

    /**
     * Function for searching in the existing results. It outputs all the known data about a person.
     *
     * @throws IOException if the file is not found
     */
    public static void search() throws IOException {
        PersonIndex index = new PersonIndex();

        // load all files from the page
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

            // get all the people that somehow match the input
            ArrayList<Person> people = index.getResults(input);
            if (people.size() == 0) {
                System.out.println("No person found with name \"" + input + "\"");
            } else {
                System.out.println("Found " + people.size() + " people with name \"" + input + "\":");
                int i = 0;
                if (people.size() > 1) {
                    // give a list of the first 10 people from the results
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
                    if (si.equals("more")) {
                        // show all the results
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
                            // output all the known data about the person
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

    /**
     * Function to wait for user input to select a person from the index
     *
     * @param index   the index of people
     * @param scanner the scanner for user input
     * @return the person that matches the input or null if not found
     */
    private static IdentifiedPerson getPerson(PersonIndex index, Scanner scanner) {

        String input = "";
        while (true) {
            System.out.println("Enter name to search for:");
            input = scanner.nextLine();

            // get all the people that somehow match the input
            ArrayList<Person> people = index.getResults(input);

            if (people.size() == 0) {
                System.out.println("No person found with name \"" + input + "\"");
            } else {
                System.out.println("Found " + people.size() + " people with name \"" + input + "\":");
                int i = 0;
                if (people.size() > 1) {
                    // give a list of the first 10 people from the results
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
                    if (si.equals("more")) {
                        // show all the results
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
                            // output all the known data about the person
                            IdentifiedPerson ip = (IdentifiedPerson) people.get(i - 1);
                            return ip;
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

    /**
     * Function for checking if two people are related. It outputs whether they are related.
     *
     * @param person1 first person
     * @param person2 second person
     * @return true if they are related, false otherwise
     * @throws IOException if the file is not found
     */
    public static boolean relatives(PersonIndex index, IdentifiedPerson person1, IdentifiedPerson person2) throws IOException {
        if (index == null) {
            index = new PersonIndex();

            // load all known data from the serialized file
            index.readFromFile();
        }

        Scanner scanner = new Scanner(System.in);

        // load the two people to check their relationship
        if (person1 == null) {
            System.out.println("PERSON 1");
            person1 = getPerson(index, scanner);
        }
        if (person2 == null) {
            System.out.println("PERSON 2");
            person2 = getPerson(index, scanner);
        }

        ArrayList<IdentifiedPerson> peopleToSearch = new ArrayList<>();
        peopleToSearch.add(person1); // add the first person to the list of people to search (start from here)
        ArrayList<IdentifiedPerson> searchedPeople = new ArrayList<>();

        boolean found = false;

        // run breadth first search to find the second person
        while (peopleToSearch.size() > 0) {
            IdentifiedPerson person = peopleToSearch.get(0);
            peopleToSearch.remove(0);
            searchedPeople.add(person);
            if (person.equals(person2)) {
                found = true;
                break;
            }
            // from the list of all relatives, if not already searched, add to the list of people to search
            for (IdentifiedPerson relative : person.getRelatives()) {
                if (!searchedPeople.contains(relative) && !peopleToSearch.contains(relative)) {
                    peopleToSearch.add(relative);
                }
            }
        }

        // output the result
        if (found) {
            System.out.println("Found a connection between " + person1.getName() + " and " + person2.getName());
            return true;
        } else {
            System.out.println("No connection found between " + person1.getName() + " and " + person2.getName());
            return false;
        }

    }

}