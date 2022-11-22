package sk.vighnorbert;

public class Person {
    protected final String name;

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
