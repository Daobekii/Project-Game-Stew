import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class TestGamesDatabase {
    private static GamesDatabase db;
    private static Connection conn;

    @BeforeEach
    public void createTestDB() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e){
            // do something
        }

        // get GamesDatabase instance
        // should not throw any exceptions
        assertDoesNotThrow(() -> {
            db = GamesDatabase.getInstance();
        });

        // connect to test database instead
        // we do this by using reflection to change the private
        // conn field of GamesDatabase
        try {
            // create test connection
            conn = DriverManager.getConnection("jdbc:sqlite:gamesDBTest.db");

            // delete existing tables
            Statement statement = conn.createStatement();
            statement.execute("DROP TABLE IF EXISTS games");
            statement.execute("DROP TABLE IF EXISTS gameProperties");
            statement.execute("DROP TABLE IF EXISTS gamePropertyMap");

            // replace the conn field value
            Field connField = GamesDatabase.class.getDeclaredField("conn");
            connField.setAccessible(true);
            connField.set(db, conn);

            // create new empty tables
            db.createTables();
        } catch (NoSuchFieldException | SQLException | IllegalAccessException e) {
            System.out.println("Could not connect to test database!");
            e.printStackTrace();
        }
    }

    @Test
    public void testGetGamesCount() throws SQLException {
        assertEquals(0, db.getGameCount()); // should return 0 for empty database

        // add a game
        Statement statement = conn.createStatement();
        String query = """
            INSERT INTO games(id, name, description, year, minPlayers, maxPlayers, minAge, playTime, rating, weight)
            VALUES(0, 'Test Game', 'Test', 1999, 1, 4, 0, 60, 1.23, 2.22);
        """;
        statement.execute(query);

        assertEquals(1, db.getGameCount()); // test with 1 game

        // add another game
        query = """
            INSERT INTO games(id, name, description, year, minPlayers, maxPlayers, minAge, playTime, rating, weight)
            VALUES(1, 'Test Game 2', 'Test', 1999, 1, 4, 0, 60, 1.23, 2.22);
        """;
        statement.execute(query);

        assertEquals(2, db.getGameCount()); // test with 2 games
    }

    @Test
    public void testScrapeGameIDs() throws SQLException, ParserConfigurationException, URISyntaxException, IOException, SAXException {
        // scrape the info on two games
        db.scrapeGameIDs(new int[] {224517, 13});

        assertTrue(db.hasGame(13)); // should contain the game CATAN
        assertTrue(db.hasGame(224517)); // should contain the game Brass: Birmingham
    }
}