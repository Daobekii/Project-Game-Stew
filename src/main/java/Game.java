import java.util.ArrayList;

public class Game {
    private int id;
    private String name;
    private String description;
    private int minPlayers;
    private int maxPlayers;
    private int playtime;
    private int age;
    private float rating;
    private float weight;
    private int rank;
    private int year;
    private String coverUrl;
    private String[] images;
    private ArrayList<GameProperty> properties;

    Game(int id, String name, String description, int minPlayers, int maxPlayers, int playtime, int age, String coverUrl, String[] images, float rating, float weight) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.playtime = playtime;
        this.age = age;
        this.coverUrl = coverUrl;
        this.images = images;
        this.properties = new ArrayList<>();
        this.rating = rating;
        this.weight = weight;
        this.rank = 999999999; // this is an unranked game
    }

    Game(int id, String name, String description, int minPlayers, int maxPlayers, int playtime, int age, String coverUrl, String[] images, float rating, float weight, int rank, int year) {
        this(id, name, description, minPlayers, maxPlayers, playtime, age, coverUrl, images, rating, weight);
        this.rank = rank;
        this.year = year;
    }

    public int getID() { return this.id; }
    public String getName() { return this.name; }
    public String getImageUrl() { return this.coverUrl; }
    public int getMinPlayers() { return minPlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public int getPlaytime() { return playtime; }
    public float getRating() { return rating; }
    public float getRoundedRating() { return Math.round(rating * 10f) / 10f; }
    public float getWeight() { return weight; }
    public float getRoundedWeight() { return Math.round(weight * 10f) / 10f; }
    public String getDescription() { return this.description; }
    public int getYear() { return this.year; }
    public int getAge() { return this.age; }
    public ArrayList<GameProperty> getProperties() { return properties; }

    /**
     * Checks if this game is equal to another game.
     * Comparing objects using == will only work if they have the same memory address.
     * This function will return true whenever the games share the same id, however.
     * @param o The other game
     * @return {@code true} if this game has the same id as the o argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game other = (Game) o;
        return this.id == other.id;
    }

    /**
     * Adds a GameProperty to the game
     * @param property the property
     */
    public void addProperty(GameProperty property) {
        properties.add(property);
    }
}