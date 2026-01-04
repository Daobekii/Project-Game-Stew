import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class TestStew {
    @Test
    public void testGetSimilarity() throws SQLException {
        // create test objects
        Stew stew = new Stew();
        GamesDatabase db = GamesDatabase.getInstance();
        Game catan = db.getGameByID(13);
        Game rummy = db.getGameByID(15878);
        Game wingspan = db.getGameByID(266192);
        Game wyrmspan = db.getGameByID(410201);

        // add a few games to the stew
        stew.addItem(new GameItem(0, rummy));
        stew.addItem(new GameItem(1, wingspan));

        // calculate similarity scores
        float catanSimilarity = stew.getSimilarity(catan);
        float rummySimilarity = stew.getSimilarity(rummy);
        float wingspanSimilarity = stew.getSimilarity(wingspan);
        float wyrmspanSimilarity = stew.getSimilarity(wyrmspan);

        // all scores should be in the range of 0 to 1
        assertTrue(catanSimilarity >= 0 && catanSimilarity <= 1);
        assertTrue(rummySimilarity >= 0 && rummySimilarity <= 1);
        assertTrue(wingspanSimilarity >= 0 && wingspanSimilarity <= 1);
        assertTrue(wyrmspanSimilarity >= 0 && wyrmspanSimilarity <= 1);

        // Wyrmspan, being a direct sequel to a game in the stew, should have a higher score than unrelated catan
        assertTrue(wyrmspanSimilarity > catanSimilarity);
    }

    @Test
    public void testTaste() throws SQLException {
        // create test objects
        Stew stew = new Stew();
        GamesDatabase db = GamesDatabase.getInstance();
        Game catan = db.getGameByID(13);
        Game rummy = db.getGameByID(15878);
        Game wingspan = db.getGameByID(266192);
        Game wyrmspan = db.getGameByID(410201);

        // add games to the stew
        stew.addItem(new GameItem(0, catan));
        stew.addItem(new GameItem(1, rummy));
        stew.addItem(new GameItem(2, wingspan));
        stew.addItem(new GameItem(3, wyrmspan));

        // should give a recommendation with a good score (>= 0.75)
        Recommendation rec = stew.taste();
        Game game = rec.getGame();
        float similarity = stew.getSimilarity(game);
        System.out.println(game.getName() + ": " + similarity);
        assertTrue(similarity >= 0.75);
    }
}
