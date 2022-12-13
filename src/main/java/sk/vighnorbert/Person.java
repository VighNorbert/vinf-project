package sk.vighnorbert;

import java.io.Serializable;

/**
 * Class Person holding all the information about a person.
 * This class holds people which are not part of the Wikipedia dump.
 *
 * @author Norbert Vígh
 */
public class Person implements Serializable {
    /**
     * Name of the person
     */
    protected final String name;

    /**
     * Whether the person is already serialized
     */
    public boolean serialized = false;

    public Person(String name) {
        this.name = name;
    }

    /**
     * Getter for the name
     *
     * @return name of the person
     */
    public String getName() {
        return name;
    }

    /**
     * Checker if the person is the same object
     *
     * @param o object to compare
     * @return true if the objects are the same, false otherwise
     */
    public boolean equals(Object o) {
        if (o instanceof Person) {
            return name.equals(((Person) o).getName());
        }
        return false;
    }
}
