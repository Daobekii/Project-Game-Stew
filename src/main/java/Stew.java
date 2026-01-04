import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Stew {
    private ArrayList<StewFilter> filters = new ArrayList<>();
    private ArrayList<StewItem> items = new ArrayList<>();

    /**
     * Adds a StewItem to the stew
     * @param item the StewItem
     */
    public void addItem(StewItem item) {
        items.add(item);
    }

    /**
     * Removes a StewItem from the stew
     * @param item the StewItem
     */
    public void removeItem(StewItem item) {
        items.remove(item);
    }

    /**
     * Removes all items with a given ID from the stew
     * @param id the ID
     */
    public void removeItemByID(int id) {
        items.removeIf(item -> item.getID() == id);
    }

    /**
     * Empties the stew, removing all StewItems
     */
    public void empty() {
        items.clear();
    }

    /**
     * Adds a StewFilter to the stew
     * @param filter the StewFilter
     */
    public void addFilter(StewFilter filter) {
        filters.add(filter);
    }

    /**
     * Removes a StewFilter from the stew
     * @param filter the StewFilter
     */
    public void removeFilter(StewFilter filter) {
        filters.remove(filter);
    }

    /**
     * Removes all StewFilters
     */
    public void clearFilters() {
        filters.clear();
    }

    /**
     * Calculates and returns a measure of the total similarity of the target Game with the stew.
     * The similarity score is a weighted average of scores from all StewItems in the stew.
     * 
     * @param target the Game to compare with
     * @return the similarity score as a float between 0 and 1, where 1 indicates a perfect match.
     */
    public float getSimilarity(Game target) {
        float totalScore = 0;
        float maxTotalScore = 0;

        for (StewItem item : items) {
            totalScore += item.getScore(target);
            maxTotalScore += item.getWeight();
        }

        if (maxTotalScore == 0) return 0;

        // Normalize the total score
        return totalScore / maxTotalScore;
    }

    /**
     * Returns the IDs corresponding to the games of any GameItems in the stew
     * @return the list of IDs
     */
    public List<Integer> getGameIDs() {
        List<Integer> gameIDs = new ArrayList<>();
        for (StewItem item : items) {
            if (item instanceof GameItem gameItem) {
                gameIDs.add(gameItem.getGame().getID());
            }
        }
        return gameIDs;
    }

    /**
     * Returns the list of StewItems
     * @return the list of StewItems
     */
    public ArrayList<StewItem> getItems() {
        return items;
    }

    /**
     * Gets a number of games from the database, ranks them by their similarity to the stew, and
     * creates a Recommendation from the best one, which is returned.
     * @return the Recommendation
     */
    public Recommendation taste() throws SQLException {
        GamesDatabase db = GamesDatabase.getInstance();
        List<Game> games = db.getFilteredGames(this.filters, getGameIDs(), 1000);
        Game best = null;
        float bestScore = -1;
        for (Game game : games) {
            float score = getSimilarity(game);
            if (score > bestScore) {
                bestScore = score;
                best = game;
            }
        }
        // No game found for filters!
        if (best == null) {
            return null;
        }

        ArrayList<Commonality> commonalities = new ArrayList<>();
        for (StewItem item : items) {
            commonalities.addAll(item.getCommonalities(best));
        }

        return new Recommendation(best, commonalities);
    }

    /**
     * Adds all games in the stew to a collection
     * @param collection the collection to add to
     */
    public void addToCollection(Collection collection) {
        // TODO: implement
    }
}
