import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.sqlite.Function;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GamesDatabase {
    private static GamesDatabase instance;
    private final Connection conn;

    private GamesDatabase(Connection conn) throws SQLException {
        this.conn = conn;
        createTables();
    }

    /**
     * Returns (and creates if it doesn't exist) the instance of GamesDatabase
     * Since GamesDatabase is a singleton class, there is always only ONE instance
     * of the class.
     *
     * @return {@code instance} The instance of GamesDatabase.
     */
    public static GamesDatabase getInstance() throws SQLException {
        if (instance == null) {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:gamesDB.db");
            instance = new GamesDatabase(connection);

            // create custom levenshtein distance function
            // implementation based on Wagner-Fischer algorithm
            // (see https://en.wikipedia.org/wiki/Levenshtein_distance#Iterative_with_full_matrix)
            Function.create(connection, "levenshtein", new Function() {
                protected void xFunc() throws SQLException {
                    if(args() != 2) throw new SQLException();
                    String s = value_text(0).toUpperCase();
                    String t = value_text(1).toUpperCase();

                    int[][] d = new int[s.length()+1][t.length()+1];

                    for (int i = 1; i <= s.length(); i++) {
                        d[i][0] = i;
                    }
                    for (int j = 1; j <= t.length(); j++) {
                        d[0][j] = j;
                    }
                    for (int j = 1; j <= t.length(); j++) {
                        for (int i = 1; i <= s.length(); i++) {
                            int substitutionCost = (s.charAt(i-1) == t.charAt(j-1)) ? 0 : 1;
                            d[i][j] =  Math.min(Math.min(d[i-1][j] + 1, d[i][j-1] + 1), d[i-1][j-1] + substitutionCost);
                        }
                    }

                    result(d[s.length()][t.length()]);
                }
            });
        }
        return instance;
    }

    /**
     * Creates the required games, gameProperties, and gamePropertyMap tables
     * if they don't exist.
     */
    public void createTables() throws SQLException {
        Statement statement = conn.createStatement();
        statement.execute("""
            CREATE TABLE IF NOT EXISTS games (
                id INTEGER PRIMARY KEY,
                name VARCHAR(255),
                description TEXT,
                year INTEGER,
                minPlayers INTEGER,
                maxPlayers INTEGER,
                minAge INTEGER,
                playTime INTEGER,
                rating FLOAT,
                weight FLOAT,
                coverURL VARCHAR(255),
                rank INTEGER
            );
        """);
        statement.execute("""
            CREATE TABLE IF NOT EXISTS gameProperties (
                id INTEGER PRIMARY KEY,
                type VARCHAR(255),
                subtype VARCHAR(255),
                value VARCHAR(255)
            );
        """);
        statement.execute("""
            CREATE TABLE IF NOT EXISTS gamePropertyMap (
                gameId INTEGER NOT NULL,
                propertyId INTEGER NOT NULL,
                FOREIGN KEY (gameId) REFERENCES games(id),
                FOREIGN KEY (propertyId) REFERENCES gameProperties(id),
                PRIMARY KEY (gameId, propertyId)
            );
        """);
    }

    /**
     * Returns the number of games currently in the games database
     *
     * @return {@code count} the number of games
     */
    public int getGameCount() throws SQLException {
        String query = "SELECT COUNT(*) AS gamesCount FROM games";
        Statement statement = conn.createStatement();
        ResultSet results = statement.executeQuery(query);
        results.next();
        int count = results.getInt("gamesCount");
        results.close();
        return count;
    }

    /**
     * Checks whether a game with the given ID exists in the database
     *
     * @param gameID the ID to search for
     * @return {@code true} if the game is in the database, {@code false} otherwise
     */
    public boolean hasGame(int gameID) throws SQLException {
        String query = String.format("SELECT EXISTS(SELECT * FROM games where id = %d) as hasID;", gameID);
        Statement statement = conn.createStatement();
        ResultSet results = statement.executeQuery(query);
        results.next();
        return results.getBoolean("hasID");
    }

    /**
     * Given the XML element representing a game property, adds the given property to
     * the database if it is a tracked property.
     *
     * @param propElement the XML element
     * @return The {@code id} of the property, if it is tracked, {@code null} otherwise.
     */
    private Integer parseProperty(Element propElement) throws SQLException {
        String type = propElement.getAttribute("type");
        String value = propElement.getAttribute("value");
        String[] splitValue = value.split(": ", 2);

        // property attributes
        int id = Integer.parseInt(propElement.getAttribute("id"));
        String subType = splitValue.length > 1 ? splitValue[0] : null;
        String propValue = splitValue.length > 1 ? splitValue[1] : value;
        String propType;

        switch (type) {
            case "boardgamecategory":
                propType = "category";
                break;
            case "boardgamemechanic":
                propType = "mechanic";
                break;
            case "boardgamefamily":
                propType = "family";
                break;
            case "boardgamedesigner":
                propType = "designer";
                break;
            case "boardgameartist":
                propType = "artist";
                break;
            case "boardgamepublisher":
                propType = "publisher";
                break;
            default:
                return null;
        }

        PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO gameProperties (id, type, subtype, value)
                VALUES (?, ?, ?, ?)
                ON CONFLICT DO NOTHING;
        """);
        ps.setInt(1, id);
        ps.setString(2, propType);
        ps.setString(3, subType);
        ps.setString(4, propValue);
        ps.execute();
        ps.close();

        return id;
    }

    /**
     * Given the XML element representing a game, adds the game, as well as all its
     * properties and the property mappings to the database
     *
     * @param gameItem the XML element
     */
    private void parseGame(Element gameItem) throws SQLException {
        // game properties
        int id;
        int year = 0;
        int minPlayers = 0;
        int maxPlayers = 999;
        int minAge = 0;
        int playTime = 0;
        String name = "UNNAMED";
        String coverURL = "NONE";
        String description = "";
        float rating, weight;
        int rank;

        // get simple properties
        id = Integer.parseInt(gameItem.getAttribute("id"));
        Element descriptionItem = (Element) gameItem.getElementsByTagName("description").item(0);
        if (descriptionItem != null) description = descriptionItem.getTextContent();

        Element coverImageElement = (Element) gameItem.getElementsByTagName("image").item(0);
        if (coverImageElement != null) {
            coverURL = coverImageElement.getTextContent();
        }

        // get properties that are stored as node attributes
        Element yearElement = (Element) gameItem.getElementsByTagName("yearpublished").item(0);
        if (yearElement != null) year = Integer.parseInt(yearElement.getAttribute("value"));

        Element playTimeElement = (Element) gameItem.getElementsByTagName("playingtime").item(0);
        if (playTimeElement != null) playTime = Integer.parseInt(playTimeElement.getAttribute("value"));

        Element minAgeElement = (Element) gameItem.getElementsByTagName("minage").item(0);
        if (minAgeElement != null) minAge = Integer.parseInt(minAgeElement.getAttribute("value"));

        Element minPlayersElement = (Element) gameItem.getElementsByTagName("minplayers").item(0);
        if (minPlayersElement != null) minPlayers = Integer.parseInt(minPlayersElement.getAttribute("value"));

        Element maxPlayersElement = (Element) gameItem.getElementsByTagName("maxplayers").item(0);
        if (maxPlayersElement != null) maxPlayers = Integer.parseInt(maxPlayersElement.getAttribute("value"));

        // get statistics
        Element statistics = (Element) gameItem.getElementsByTagName("statistics").item(0);
        Element ratingAvg = (Element) statistics.getElementsByTagName("average").item(0);
        Element weightAvg = (Element) statistics.getElementsByTagName("averageweight").item(0);
        Element rankElement = (Element) statistics.getElementsByTagName("rank").item(0);
        try {
            rating = Float.parseFloat(ratingAvg.getAttribute("value"));
        } catch (NumberFormatException e) {
            rating = -1;
        }
        try {
            weight = Float.parseFloat(weightAvg.getAttribute("value"));
        } catch (NumberFormatException e) {
            weight = -1;
        }
        try {
            rank = Integer.parseInt(rankElement.getAttribute("value"));
        } catch (NumberFormatException | NullPointerException e) {
            rank = 999999;
        }

        // find game name
        NodeList nameElements = gameItem.getElementsByTagName("name");
        for (int j = 0; j < nameElements.getLength(); j++) {
            Element nameElement = (Element) nameElements.item(j);
            if (nameElement.getAttribute("type").equals("primary")) {
                name = nameElement.getAttribute("value");
                break;
            }
        }

        // create game database entry
        PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO games (id, name, description, year, minPlayers, maxPlayers, minAge, playTime, rating, weight, coverURL, rank)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT DO NOTHING;
        """);
        ps.setInt(1, id);
        ps.setString(2, name);
        ps.setString(3, description);
        ps.setInt(4, year);
        ps.setInt(5, minPlayers);
        ps.setInt(6, maxPlayers);
        ps.setInt(7, minAge);
        ps.setFloat(8, playTime);
        ps.setFloat(9, rating);
        ps.setFloat(10, weight);
        ps.setString(11, coverURL);
        ps.setInt(12, rank);
        ps.executeUpdate();
        ps.close();

        // handle game properties
        NodeList gameProperties = gameItem.getElementsByTagName("link");
        for (int j = 0; j < gameProperties.getLength(); j++) {
            Element propElement = (Element) gameProperties.item(j);
            Integer propID = parseProperty(propElement);
            if (propID != null) {
                // this is a tracked property, add a mapping to the database
                PreparedStatement mapPS = conn.prepareStatement("""
                    INSERT INTO gamePropertyMap (gameId, propertyId)
                    VALUES (?, ?)
                    ON CONFLICT DO NOTHING;
                """);
                mapPS.setInt(1, id);
                mapPS.setInt(2, propID);
                mapPS.execute();
                mapPS.close();
            }
        }
    }

    /**
     * Given an array of game IDs, scrapes the information on those games, as well as all their
     * properties from the BoardGameGeek API and adds them to the database.
     *
     * @param ids The array of game IDs to scrape
     */
    public void scrapeGameIDs(int[] ids) throws ParserConfigurationException, URISyntaxException, IOException, SAXException, SQLException {
        // construct request URL
        StringJoiner idsJoiner = new StringJoiner(",");
        for (int id : ids) {
            idsJoiner.add(String.valueOf(id));
        }
        String url = "https://boardgamegeek.com/xmlapi2/thing?type=boardgame&stats=1&id=" + idsJoiner;

        // get XML Document
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new URI(url).toURL().openStream());

        // parse the games
        NodeList items = doc.getElementsByTagName("item");
        for (int i = 0; i < items.getLength(); i++) {
            // game ID, description, year
            Element item = (Element) items.item(i);
            if (!item.getAttribute("type").equals("boardgame")) continue;
            parseGame(item);
        }
    }

    public void scrapePopularGames(int batchSize, int numGames) throws SQLException, ParserConfigurationException, URISyntaxException, IOException, SAXException, InterruptedException {
        // read popular games CSV
        List<String[]> data = new ArrayList<>();
        try (InputStream input = this.getClass().getResourceAsStream("boardgame_ranks.csv")) {
            assert input != null;
            CSVReader reader = new CSVReader(new InputStreamReader(input));
            data = reader.readAll();
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't find boardgame_ranks.csv! Download from https://boardgamegeek.com/data_dumps/bg_ranks");
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }

        // actually start scraping
        int maxBatchSize = batchSize;
        List<Integer> batch = new ArrayList<>();
        numGames = Math.min(numGames, data.size());
        for (int i = 1; i < data.size(); i++) {
            if (i > numGames) return;
            String[] line = data.get(i);
            batch.add(Integer.parseInt(line[0]));
            if (batch.size() >= batchSize || i == data.size() - 1 || i == numGames) {
                try {
                    scrapeGameIDs(batch.stream().mapToInt(Integer::intValue).toArray());
                    System.out.println("Scraped " + batch.size() + " games (" + i + "/" + numGames + ")! Increasing batch size!");
                    batchSize = Math.min(maxBatchSize, (int) (batchSize * 1.25));
                } catch (IOException e) {
                    System.out.println("An error occurred scraping games #" + (i - batch.size()) + " - " + i + "!");
                    System.out.println("Error: " + e.getMessage());
                    System.out.println("Waiting 10 seconds, then retrying with lower batch size!");
                    Thread.sleep(10000);
                    batchSize = Math.max(10, batchSize/2);
                    i -= batch.size();
                }
                batch.clear();
            }
        }
    }

    /**
     * Scrapes the BGG games with IDs from startID to startID + numGames,
     * adding the games and properties to the database.
     * @param startID the game ID to start scraping at
     * @param numGames the number of games to scrape
     * @param batchSize the number of games to scrape in a single request. 1000 is reasonable.
     */
    public void scrapeGames(int startID, int numGames, int batchSize) throws SQLException, ParserConfigurationException, URISyntaxException, SAXException, InterruptedException {
        int maxBatchSize = batchSize;
        int count = 0;
        while (count < numGames) {
            int numBatchIDs = Math.min(batchSize, numGames - count);
            int[] batchIDs = IntStream.range(startID + count, startID + count + numBatchIDs).toArray();
            try {
                scrapeGameIDs(batchIDs);
            } catch (IOException e) {
                System.out.println("An error occurred scraping games " + (startID + count) + " - " + (startID + count + numBatchIDs) + "!");
                System.out.println("Error: " + e.getMessage());
                System.out.println("Waiting 10 seconds, then retrying with lower batch size!");
                Thread.sleep(10000);
                batchSize = Math.max(10, batchSize/2);
                continue;
            }
            count += numBatchIDs;
            System.out.println("Scraped " + numBatchIDs + " games (" + count + "/" + numGames + "), increasing batch size!");
            batchSize = Math.min(maxBatchSize, (int) (batchSize * 1.25));
            Thread.sleep(250); // sleep to adhere to API rate limits
        }
    }

    /**
     * Given a ResultSet of games, returns the game corresponding to the first element fo the set.
     * @param rs the ResultSet
     * @return the Game if there is one, {@code null} otherwise
     */
    public Game getGameFromResultSet(ResultSet rs) throws SQLException {
        Game game;

        if (rs.next()) {
            game = new Game(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getInt("minPlayers"),
                rs.getInt("maxPlayers"),
                rs.getInt("playTime"),
                rs.getInt("minAge"),
                rs.getString("coverURL"),
                new String[] {},
                rs.getFloat("rating"),
                rs.getFloat("weight"),
                rs.getInt("rank"),
                rs.getInt("year")
            );
        } else {
            return null;
        }

        // get game properties
        PreparedStatement psGetProperties = conn.prepareStatement("""
            SELECT
                DISTINCT p.*
            FROM gameProperties p
                JOIN gamePropertyMap m ON p.id = m.propertyId
                JOIN games g ON m.gameId = g.id
            WHERE g.id = ?
        """);
        psGetProperties.setInt(1, game.getID());
        ResultSet rsGetProperties = psGetProperties.executeQuery();
        GameProperty prop;

        while ((prop = getPropertyFromResultSet(rsGetProperties)) != null) {
            game.addProperty(prop);
        }

        return game;
    }

    /**
     * Given a ResultSet, returns the property corresponding to the first line.
     * @param rs the ResultSet
     * @return the GameProperty, if any, otherwise {@code null}
     */
    public GameProperty getPropertyFromResultSet(ResultSet rs) throws SQLException {
        GameProperty prop;

        if (rs.next()) {
            prop = new GameProperty(
                rs.getInt("id"),
                rs.getString("type"),
                rs.getString("subtype"),
                rs.getString("value")
            );
        } else {
            return null;
        }

        return prop;
    }

    /**
     * Queries the database for a game with a given id.
     * @param id the game's ID
     * @return {@code game} if the game is in the database, {@code null} otherwise
     */
    public Game getGameByID(int id) throws SQLException {
        Game game;
        // query game information from database
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM games WHERE id = ?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        return getGameFromResultSet(rs);
    }

    /**
     * Queries the database and returns a GameProperty with a given ID.
     * @param id the ID to search for
     * @return the GameProperty, if any, {@code null} otherwise
     * @throws SQLException
     */
    public GameProperty getPropertyByID(int id) throws SQLException {
        GameProperty prop;
        // query property information from database
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM gameProperties WHERE id = ?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        return getPropertyFromResultSet(rs);
    }

    /**
     * Returns a filtered list of games from the database, sorted by rank and rating.
     * @param filters the list of StewFilters to filter with
     * @param excludeIDs a list of game IDs to exclude
     * @param limit the maximum number of games to get
     * @return the list of games
     */
    public ArrayList<Game> getFilteredGames(List<StewFilter> filters, List<Integer> excludeIDs, int limit) throws SQLException {
        excludeIDs.add(-1); // add this so there's always *something* to exclude
        String excludeArray = excludeIDs.stream().map(String::valueOf).collect(Collectors.joining(","));

        ArrayList<Game> games = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM games WHERE TRUE");
        for (StewFilter filter : filters) {
            String filterQuery = filter.getQuery();
            if (!filterQuery.isBlank()) {
                query.append(" AND ").append(filter.getQuery());
            }
        }
        query.append(" AND id NOT IN (").append(excludeArray).append(") ORDER BY rank, rating DESC LIMIT ?");
        PreparedStatement statement = conn.prepareStatement(query.toString());

        statement.setInt(1, limit);
        ResultSet rs = statement.executeQuery();
        Game currentGame;
        while ((currentGame = getGameFromResultSet(rs)) != null) {
            games.add(currentGame);
        }
        return games;
    }

    /**
     * Returns a list of games whose name is similar to partialName, sorted in a reasonable way.
     *
     * @param partialName the partial name string to search for
     * @param numGames the maximum number of games to return
     * @return the list of games
     */
    public List<Game> searchGamesByName(String partialName, int numGames) throws SQLException {
        String query = """
            SELECT * FROM games
            ORDER BY
                CASE WHEN name LIKE ? THEN 0 ELSE levenshtein(?, name) END, LENGTH(name), rank, rating
            LIMIT ?
        """;
        PreparedStatement statement = conn.prepareStatement(query);
        statement.setString(1, "%" + partialName + "%");
        statement.setString(2, partialName);
        statement.setInt(3, numGames);

        ResultSet rs = statement.executeQuery();
        List<Game> games = new ArrayList<>();
        Game currentGame;
        while ((currentGame = getGameFromResultSet(rs)) != null) {
            games.add(currentGame);
        }
        return games;
    }

    public static void main(String[] args) throws SQLException, ParserConfigurationException, URISyntaxException, IOException, InterruptedException, SAXException {
        GamesDatabase db = GamesDatabase.getInstance();
        List<Game> games = db.searchGamesByName("monpoly", 5);
        for (Game game : games) {
            System.out.println(game.getName());
        }
    }
}