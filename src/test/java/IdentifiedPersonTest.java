import org.junit.jupiter.api.Test;
import sk.vighnorbert.IdentifiedPerson;
import sk.vighnorbert.Person;
import sk.vighnorbert.PersonIndex;

import static org.junit.jupiter.api.Assertions.*;

class IdentifiedPersonTest {

    @Test
    void testRunCheckBack() {
        PersonIndex pi = new PersonIndex();
        IdentifiedPerson p = new IdentifiedPerson("John Doe");
        pi.addPerson(p);

        IdentifiedPerson parent = new IdentifiedPerson("John Doe Sr.");
        pi.addPerson(parent);

        p.getMh().parentMatches.add(new Person("John Doe Sr."));

        p.getMh().childMatches.add(new Person("John Doe Jr."));

        p.runBackCheck(pi);

        assertTrue(p.getParents().contains(parent));

        assertFalse(p.getChildren().contains(parent));
    }

}