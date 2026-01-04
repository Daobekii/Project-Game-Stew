import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestCollection {
    @Test
    public void testContainsGame() {
        // create test collection
        Collection coll = new Collection(0, SpecialRole.NONE, "Example Collection", "example.png");

        // create test games
        Game game1 = new Game(0, "Test1", "", 1, 4, 60, 6, "example.png", new String[]{}, 0, 0);
        Game game2 = new Game(1, "Test2", "", 1, 4, 60, 6, "example.png", new String[]{}, 0, 0);

        // insert some of the test games
        coll.addGame(game1);

        // run actual tests
        assertTrue(coll.containsGame(game1));   // game contained in collection
        assertFalse(coll.containsGame(game2));  // game not contained in collection
        assertTrue(coll.containsGame(
                new Game(0, "Test1", "", 1, 4, 60, 6, "example.png", new String[]{}, 0, 0)
        ));                                     // collection contains this game, but not the exact same object
    }

    @Test
    public void testContainsID() {
        // create test collection
        Collection coll = new Collection(0, SpecialRole.NONE, "Example Collection", "example.png");

        // create and insert test game
        Game game1 = new Game(0, "Test1", "", 1, 4, 60, 6, "example.png", new String[]{}, 0, 0);
        coll.addGame(game1);

        // run tests
        assertTrue(coll.containsID(0));     // ID contained in collection
        assertFalse(coll.containsID(1));    // ID not contained in collection
    }
}
