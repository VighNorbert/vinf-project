package sk.vighnorbert;

import java.io.Serializable;

public class Person implements Serializable {
    protected final String name;

    public boolean serialized = false;

    public Person(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public boolean equals(Object o) {
        if (o instanceof Person) {
            return name.equals(((Person) o).getName());
        }
        return false;
    }
}