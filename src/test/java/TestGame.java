import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestGame {
    @Test
    public void testEquals() {
        // create test objects
        Game game1 = new Game(0, "Test1", "", 1, 4, 60, 6, "example.png", new String[]{}, 0, 0);
        Game game2 = new Game(1, "Test2", "", 1, 4, 60, 6, "example.png", new String[]{}, 0, 0);
        Game game3 = new Game(0, "Test3", "", 5, 100, 5, 99, "example.png", new String[]{}, 0, 0);
        Game game4 = new Game(0, "Test1", "", 1, 4, 60, 6, "example.png", new String[]{}, 0, 0);
        String testString = "Test1";

        // run tests
        assertEquals(game1, game1);             // should equal itself
        assertNotEquals(game1, null);    // should not equal null
        assertNotEquals(game1, testString);     // should not equal an object of another type
        assertNotEquals(game2, game1);          // should not equal a different game
        assertEquals(game1, game4);             // should equal a different game object with the same ID
        assertEquals(game1, game3);             // should equal a different game object with the same ID, even if other properties are different
    }
}