import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sk.vighnorbert.IdentifiedPerson;
import sk.vighnorbert.Main;
import sk.vighnorbert.PersonIndex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {
    @Test
    void testrelatives() throws IOException {
        PersonIndex pi = new PersonIndex();

        IdentifiedPerson johnDoe = new IdentifiedPerson("John Doe");
        pi.addPerson(johnDoe);

        IdentifiedPerson johnDoeSr = new IdentifiedPerson("John Doe Sr.");
        johnDoeSr.addChild(johnDoe);
        johnDoe.addParent(johnDoeSr);
        pi.addPerson(johnDoeSr);

        IdentifiedPerson amandaDoe = new IdentifiedPerson("Amanda Doe");
        amandaDoe.addChild(johnDoe);
        johnDoe.addParent(amandaDoe);
        amandaDoe.addSpouse(johnDoeSr);
        johnDoeSr.addSpouse(amandaDoe);
        pi.addPerson(amandaDoe);

        IdentifiedPerson steveSmith = new IdentifiedPerson("Steve Smith");
        pi.addPerson(steveSmith);

        assertTrue(Main.relatives(pi, johnDoe, johnDoeSr));
        assertTrue(Main.relatives(pi, johnDoeSr, johnDoe));

        assertTrue(Main.relatives(pi, johnDoe, amandaDoe));
        assertTrue(Main.relatives(pi, amandaDoe, johnDoe));

        assertTrue(Main.relatives(pi, amandaDoe, johnDoeSr));
        assertTrue(Main.relatives(pi, johnDoeSr, amandaDoe));

        assertFalse(Main.relatives(pi, steveSmith, johnDoe));
        assertFalse(Main.relatives(pi, steveSmith, johnDoeSr));
        assertFalse(Main.relatives(pi, steveSmith, amandaDoe));
        assertFalse(Main.relatives(pi, johnDoe, steveSmith));
        assertFalse(Main.relatives(pi, johnDoeSr, steveSmith));
        assertFalse(Main.relatives(pi, amandaDoe, steveSmith));
    }

    @BeforeAll
    static void beforeAll() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("extracted-data.out"));
            br.readLine();
        } catch (IOException e) {
            try {
                Main.parse();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }
}
