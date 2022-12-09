import org.junit.jupiter.api.Test;
import sk.vighnorbert.IdentifiedPerson;
import sk.vighnorbert.Person;
import sk.vighnorbert.PersonIndex;

import static org.junit.jupiter.api.Assertions.*;

public class PersonIndexTest {
    @Test
    void testGetPerson() {
        PersonIndex pi = new PersonIndex();

        Person p = pi.isPerson("John Doe");
        assertNull(p);

        pi.addPerson(new IdentifiedPerson("John Doe"));

        p = pi.isPerson("John Doe");
        assertNotNull(p);
        assertEquals("John Doe", p.getName());

    }
    @Test
    void testSerializeCollection() {
        PersonIndex pi = new PersonIndex();

        IdentifiedPerson johnDoe = new IdentifiedPerson("John Doe");
        pi.addPerson(johnDoe);

        IdentifiedPerson johnDoeSr = new IdentifiedPerson("John Doe Sr.");
        johnDoeSr.addChild(johnDoe);
        johnDoe.addParent(johnDoeSr);
        pi.addPerson(johnDoeSr);

        assertEquals(
                "John Doe\tparent\tJohn Doe Sr.\n",
                pi.serializeCollection(johnDoe.getParents(), "parent", johnDoe.getName())
        );

    }
}
