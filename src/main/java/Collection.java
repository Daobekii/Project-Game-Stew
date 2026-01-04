import java.util.ArrayList;
import java.util.List;

public class Collection {
    private int id;
    private SpecialRole specialRole;
    private String name;
    private String icon;
    private ArrayList<Game> games = new ArrayList<Game>();

    public Collection(int id, SpecialRole specialRole, String name, String icon) {
        this.id = id;
        this.specialRole = specialRole;
        this.name = name;
        this.icon = icon;
    }

    /**
     * Checks whether a game is contained in the collection
     * @param game the game to check for
     * @return {@code true} if a game with the same ID is in the collection, {@code false} otherwise
     */
    public boolean containsGame(Game game) {
        return this.games.contains(game); // uses Game.equals internally!
    }

    /**
     * Checks whether a game with the given ID is contained in the collection
     * @param id the game ID to check for
     * @return {@code true} if a game with the given ID is in the collection, {@code false} otherwise
     */
    public boolean containsID(int id) {
        Game compGame = new Game(id, "", "", 0, 0, 0, 0, "", new String[]{}, 0, 0);
        return this.containsGame(compGame);
    }

    /**
     * Adds a game to the collection
     * @param game the Game to be added
     */
    public void addGame(Game game) {
        games.add(game);
    }

    /**
     * Removes a game from the collection
     * @param game the Game to be removed
     */
    public void removeGame(Game game) {
        games.remove(game);
    }

    /**
     * Removes all games with the given ID from the collection, if any
     * @param id the game ID to remove
     */
    public void removeGameByID(int id) {
        games.removeIf(game -> game.getID() == id);
    }

    /**
     * Gets the ID of the collection
     * @return the ID of the collection
     */
    public int getID() {
        return id;
    }

    /**
     * Returns the name of the collection
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the collection's role
     * @return the role
     */
    public SpecialRole getSpecialRole() {
        return specialRole;
    }

    /**
     * Gets the collection's icon
     * @return the icon URL
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Check whether the collection has no special role
     * @return {@code true} if the collection has the NONE role, {@code false} otherwise
     */
    public boolean hasNoSpecialRole() {
        return specialRole == SpecialRole.NONE;
    }

    /**
     * Gets the number of games in this collection
     * @return the number
     */
    public int getNumberOfGames() {
        return games.size();
    }

    /**
     * Returns the list of games in the collection
     * @return the list
     */
    public List<Game> getGames() {
        return this.games;
    }
}