import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GameItem implements StewItem {

    private int id;
    private Game game;

    public GameItem(int id, Game game){
        this.id = id;
        this.game = game;
    }

    public Game getGame() { return this.game; }

    /**
     * Calculates and returns the degree of similarity between the target Game and this item.
     *
     * @param target the Game to compare with
     * @return the similarity score
     */
    @Override
    public float getScore(Game target) {
        float score = 0;
        float maxTotal = RecommendationParameters.playerCountWeight +
                         RecommendationParameters.playtimeWeight +
                         RecommendationParameters.weightWeight +
                         RecommendationParameters.propertiesWeight;

        /* Compare basic properties */
        score += RecommendationParameters.playerCountWeight * comparePlayerCount(target);
        score += RecommendationParameters.playtimeWeight * comparePlaytime(target);
        score += RecommendationParameters.weightWeight * compareWeight(target);

        /* compare game properties */
        float propertiesScore = 0;
        float propertiesMax = 0;

        // create hash sets for faster comparisons
        HashSet<String> targetSubTypes = new HashSet<>();
        HashSet<Integer> targetProperties = new HashSet<>();

        // populate with target properties
        for (GameProperty prop : target.getProperties()) {
            targetSubTypes.add(prop.getSubtype());
            targetProperties.add(prop.getID());
        }

        // calculate properties score
        for (GameProperty prop : game.getProperties()) {
            float typeWeight = RecommendationParameters.getPropertyTypeWeight(prop.getType());
            String subtype = prop.getSubtype();
            int id = prop.getID();
            if(targetSubTypes.contains(subtype)){
                propertiesScore += typeWeight * RecommendationParameters.subTypeWeight;
                propertiesMax += typeWeight * RecommendationParameters.subTypeWeight;
            }
            if(targetProperties.contains(id)){
                propertiesScore += typeWeight * RecommendationParameters.valueWeight;
                propertiesMax += typeWeight * RecommendationParameters.valueWeight;
            }
        }
        score += RecommendationParameters.propertiesWeight * propertiesScore/propertiesMax;

        return RecommendationParameters.gameItemWeight * (score / maxTotal);
    }

    /**
     * @return the weight of the item
     */
    @Override
    public float getWeight() {
        return RecommendationParameters.gameItemWeight;
    }

    /**
     * Compares the number of players between two games and returns a similarity score.
     * The score is based on the overlap in the range of players each game supports.
     * For example, if the ranges overlap completely, the score will be 1. If there is no overlap, 
     * the score will be 0. Partial overlaps will yield a score between 0 and 1.
     * 
     * @param target the Game object to compare to.
     * @return a float representing the similarity in the number of players, where 1 is a perfect match and 0 means no overlap.
     */
    private float comparePlayerCount(Game target) {
        int min1 = game.getMinPlayers();
        int max1 = game.getMaxPlayers();
        int min2 = target.getMinPlayers();
        int max2 = target.getMaxPlayers();

        int overlapMin = Math.max(min1, min2);
        int overlapMax = Math.min(max1, max2);

        if (overlapMin > overlapMax) {
            return 0; // No overlap
        }

        // Calculate the overlap ratio
        int range1 = max1 - min1 + 1;
        int range2 = max2 - min2 + 1;
        int overlapRange = overlapMax - overlapMin + 1;

        return (float) overlapRange / (float) Math.min(range1, range2);
    }

    /**
     * Compares the game's playtime to another target game's playtime
     *
     * @param target the game to compare to
     * @return a float in the range 0-1 indicating the similarity in playtime
     */
    private float comparePlaytime(Game target) {
        float playtimeDifference = Math.abs(this.game.getPlaytime() - target.getPlaytime());
        float maximumPlaytime = Math.max(this.game.getPlaytime(), target.getPlaytime());

        if (maximumPlaytime == 0) {
            return 1; // the database is missing a playtime for both games! return to avoid a divide-by-zero error!
        }

        return 1 - (playtimeDifference / maximumPlaytime);
    }

    /**
     * Returns the normalized difference in weight between the game and another target game
     *
     * @param target the game to compare to
     * @return the difference (from 0 to 1)
     */
    private float compareWeight(Game target) {
        // divide by 5.0 to normalize the weight
        return 1 - (Math.abs(this.game.getWeight() - target.getWeight()) / 5.0f);
    }

    /**
     * Returns a list of all commonalities of the target game with this item
     * @param target the target game
     * @return the list of commonalities
     */
    @Override
    public List<Commonality> getCommonalities(Game target) {
        List<Commonality> commonalities = new ArrayList<>();
        // player count commonality
        if (comparePlayerCount(target) > 0.75) {
            commonalities.add(new Commonality(
                    this,
                    "Similar Player Count",
                    target.getMinPlayers() + " - " + target.getMaxPlayers(),
                    "https://boardgamegeek.com/boardgame/" + game.getID(),
                    -1
            ));
        }

        // playtime commonality
        if (comparePlaytime(target) > 0.75) {
            commonalities.add(new Commonality(
                    this,
                    "Similar Playtime",
                    target.getPlaytime() + " minutes",
                    "https://boardgamegeek.com/boardgame/" + game.getID(),
                    -1
            ));
        }

        // weight commonality
        if (comparePlaytime(target) > 0.75) {
            commonalities.add(new Commonality(
                    this,
                    "Similar Weight",
                    target.getWeight() + " / 5",
                    "https://boardgamegeek.com/boardgame/" + game.getID(),
                    -1
            ));
        }

        // add property commonalities
        for (GameProperty prop : target.getProperties()) {
            float typeWeight = RecommendationParameters.getPropertyTypeWeight(prop.getType());
            if (typeWeight <= 0) continue;
            for (GameProperty prop2 : game.getProperties()) {
                if (prop.getID() == prop2.getID()) {
                    commonalities.add(new Commonality(
                        this,
                        "Same " + prop.getType(),
                        prop.getCombinedValue(),
                        "https://boardgamegeek.com/boardgame/" + game.getID(),
                        prop.getID()
                    ));
                }
            }
        }
        return commonalities;
    }

    /**
     * @return the visual size of the item
     */
    @Override
    public float getSize() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSize'");
    }

    /**
     * @param size what to set the visual size of the item to
     */
    @Override
    public void setSize(float size) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setSize'");
    }

    /**
     * @return the item's image URL
     */
    @Override
    public String getImageUrl() {
        return game.getImageUrl();
    }

    /**
     * @param imageUrl Wht to set the item's image URL to
     */
    @Override
    public void setImageUrl(String imageUrl) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setImageUrl'");
    }

    /**
     * @return the item's name
     */
    @Override
    public String getName() {
        return game.getName();
    }

    /**
     * @param name the name to give the item
     */
    @Override
    public void setName(String name) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setName'");
    }

    /**
     * @return the item's ID
     */
    @Override
    public int getID() {
        return id;
    }

    /**
     * @param id the ID to give the item
     */
    @Override
    public void setID(int id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setID'");
    }
    
}
