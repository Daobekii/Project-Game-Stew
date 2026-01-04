import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Session;
import spark.template.velocity.VelocityTemplateEngine;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

public class WebServer {
    /**
     * Returns (and creates) the session associated with the request.
     * Also creates a stew and collections list, if there are none, and adds a likes and dislikes collection.
     *
     * @param req the Request
     * @return the Session
     */
    private static Session getSession(Request req) {
        Session session = req.session(true);

        // Handle stew
        if (session.attribute("stew") == null) {
            Stew s = new Stew();
            session.attribute("stew", s);
        }

        // Handle collections
        if (session.attribute("collections") == null) {
            Collection likes = new Collection(0, SpecialRole.LIKES, "Likes", "/placeholder.png");
            Collection dislikes = new Collection(1, SpecialRole.DISLIKES, "Dislikes", "/placeholder.png");
            List<Collection> coll = new ArrayList<>();
            coll.add(likes);
            coll.add(dislikes);
            session.attribute("collections", coll);
            session.attribute("likes", likes);
            session.attribute("dislikes", dislikes);
        }

        return session;
    }

    /**
     * Serves the main stew view HTML
     *
     * @param req the Request
     * @param res the Response object
     * @return the rendered HTML template for the stew view
     */
    private static String serveMainTemplate(Request req, Response res) {
        Session session = getSession(req);
        Stew stew = session.attribute("stew");
        Map<String, Object> model = new HashMap<>();
        model.put("items", stew.getItems());
        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/stew.vm"));
    }

    /**
     * Removes an item with the given ID from the stew
     *
     * @param req the Request
     * @param res the Response object
     * @return the String "success!"
     */
    private static String removeStewItem(Request req, Response res) {
        Session session = getSession(req);
        Stew stew = session.attribute("stew");
        int id = Integer.parseInt(req.queryParams("id"));
        stew.removeItemByID(id);
        return "success!";
    }

    /**
     * Fuzzy-searches for a game name, returns the results list in JSON format
     *
     * @param req the Request
     * @param res the Response Object
     * @return a JSON string with the search results.
     */
    private static String searchGame(Request req, Response res) throws SQLException {
        GamesDatabase db = GamesDatabase.getInstance();
        String query = req.queryParams("q");
        List<Game> games = db.searchGamesByName(query, 5);
        StringBuilder returnBuilder = new StringBuilder();
        returnBuilder.append("[");
        for (Game g : games) {
            returnBuilder
                    .append("{\"name\":\"")
                    .append(g.getName())
                    .append("\", \"id\":")
                    .append(g.getID())
                    .append(", \"year\":")
                    .append(g.getYear())
                    .append("},");
        }
        returnBuilder.deleteCharAt(returnBuilder.length() - 1);
        returnBuilder.append("]");
        return returnBuilder.toString();
    }

    /**
     * Adds a game with a given ID to the stew.
     *
     * @param req the Request
     * @param res the Response object
     * @return the string "success!"
     */
    private static String addStewGame(Request req, Response res) throws SQLException {
        Session session = getSession(req);
        Stew stew = session.attribute("stew");

        GamesDatabase db = GamesDatabase.getInstance();
        int id = Integer.parseInt(req.queryParams("id"));

        Game g = db.getGameByID(id);
        stew.addItem(new GameItem((int) (Math.random() * 10000000), g));

        return "success!";
    }

    /**
     * Adds a property item to the stew for the property with a given ID
     *
     * @param req the Request
     * @param res the Response object
     * @return the string "success!"
     */
    private static String addStewProperty(Request req, Response res) throws SQLException {
        Session session = getSession(req);
        Stew stew = session.attribute("stew");

        GamesDatabase db = GamesDatabase.getInstance();
        int id = Integer.parseInt(req.queryParams("id"));

        GameProperty p = db.getPropertyByID(id);
        stew.addItem(new PropertyItem((int) (Math.random() * 10000000), p));

        return "success!";
    }

    /**
     * Updates the stew filters based on the request by first clearing them,
     * then adding the new ones back in.
     *
     * @param req the Request
     * @param res the Response object
     * @return the string "success!"
     */
    private static String updateStewFilters(Request req, Response res) {
        Session session = getSession(req);
        Stew stew = session.attribute("stew");

        int minPlayers = Integer.parseInt(req.queryParams("min-players"));
        int maxPlayers = Integer.parseInt(req.queryParams("max-players"));
        int minAge = Integer.parseInt(req.queryParams("min-age"));
        int minPlaytime = Integer.parseInt(req.queryParams("min-playtime"));
        int maxPlaytime = Integer.parseInt(req.queryParams("max-playtime"));
        int minWeight = Integer.parseInt(req.queryParams("min-weight"));
        int maxWeight = Integer.parseInt(req.queryParams("max-weight"));
        int minRating = Integer.parseInt(req.queryParams("min-rating"));

        stew.clearFilters();
        stew.addFilter(new StewFilter(FilterCategory.MIN_PLAYERS, minPlayers));
        stew.addFilter(new StewFilter(FilterCategory.MAX_PLAYERS, maxPlayers));
        stew.addFilter(new StewFilter(FilterCategory.MIN_AGE, minAge));
        stew.addFilter(new StewFilter(FilterCategory.MIN_PLAYTIME, minPlaytime));
        stew.addFilter(new StewFilter(FilterCategory.MAX_PLAYTIME, maxPlaytime));
        stew.addFilter(new StewFilter(FilterCategory.MIN_WEIGHT, minWeight));
        stew.addFilter(new StewFilter(FilterCategory.MAX_WEIGHT, maxWeight));
        stew.addFilter(new StewFilter(FilterCategory.MIN_RATING, minRating));

        return "success!";
    }

    /**
     * Serves the template for stew tasting (recommendation)
     *
     * @param req the Request
     * @param res the Response object
     * @return the rendered HTML page
     */
    private static String serveTastyStew(Request req, Response res) throws SQLException {
        Session session = getSession(req);
        Stew stew = session.attribute("stew");
        Collection likes = session.attribute("likes");
        Collection dislikes = session.attribute("dislikes");

        Recommendation rec = stew.taste();

        if (rec == null) {
            res.redirect("/");
            return "No Recommendation Found!";
        }
        ;

        Game game = rec.getGame();

        Map<String, Object> model = new HashMap<>();
        model.put("game", game);
        model.put("score", stew.getSimilarity(game));
        model.put("commonalities", rec.getCommonalities());
        model.put("liked", likes.containsGame(game));
        model.put("disliked", dislikes.containsGame(game));
        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/taste.vm"));
    }

    /**
     * Serves the collections list template
     *
     * @param req the Request
     * @param res the Response Object
     * @return the rendered HTML page
     */
    private static String serveCollectionsList(Request req, Response res) throws SQLException {
        Session session = getSession(req);
        List<Collection> collections = session.attribute("collections");

        Map<String, Object> model = new HashMap<>();
        model.put("collections", collections);
        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/collectionsList.vm"));
    }

    /**
     * Creates a new collection with a given name
     *
     * @param req the Request
     * @param res the Response object
     * @return the string "success!"
     */
    private static String createCollection(Request req, Response res) {
        Session session = getSession(req);
        List<Collection> collections = session.attribute("collections");

        String name = req.queryParams("name");

        Collection newColl = new Collection((int) (Math.random() * 10000000), SpecialRole.NONE, name, "/placeholder.png");
        collections.add(newColl);

        return "success!";
    }

    /**
     * Deletes a collection with a given ID.
     * @param req the Request
     * @param res the Response object
     * @return the string "success!"
     */
    private static String deleteCollection(Request req, Response res) {
        Session session = getSession(req);
        List<Collection> collections = session.attribute("collections");

        int id = Integer.parseInt(req.queryParams("id"));
        collections.removeIf(coll -> coll.getID() == id);

        return "success!";
    }

    /**
     * Renders and returns the single collection view HTML
     * @param req the Request
     * @param res the Response object
     * @return the rendered HTML view
     */
    private static String serveCollection(Request req, Response res) {
        Session session = getSession(req);
        List<Collection> collections = session.attribute("collections");

        int id = Integer.parseInt(req.queryParams("id"));
        Collection coll = collections.stream().filter(c -> c.getID() == id).findFirst().orElse(null);

        Map<String, Object> model = new HashMap<>();
        model.put("collection", coll);
        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/collection.vm"));
    }

    /**
     * Removes a game with a given ID from a collection with a given ID.
     * @param req the Request
     * @param res the Response object
     * @return the string "success!"
     */
    private static String removeFromCollection(Request req, Response res) {
        Session session = getSession(req);
        List<Collection> collections = session.attribute("collections");

        int collID = Integer.parseInt(req.queryParams("coll"));
        int gameID = Integer.parseInt(req.queryParams("game"));

        collections.stream()
                .filter(coll -> coll.getID() == collID)
                .findFirst()
                .ifPresent(coll -> coll.removeGameByID(gameID));

        return "success!";
    }

    /**
     * Adds a game with a given ID to a collection with a given ID.
     * @param req the Request
     * @param res the Response object
     * @return the string "success!"
     */
    private static String addToCollection(Request req, Response res) throws SQLException {
        Session session = getSession(req);
        GamesDatabase db = GamesDatabase.getInstance();
        List<Collection> collections = session.attribute("collections");

        int collID = Integer.parseInt(req.queryParams("coll"));
        int gameID = Integer.parseInt(req.queryParams("game"));

        Game game = db.getGameByID(gameID);

        collections.stream()
                .filter(coll -> coll.getID() == collID)
                .findFirst()
                .ifPresent(coll -> coll.addGame(game));

        return "success!";
    }

    /**
     * Adds a game with a given ID to the Likes collection.
     * @param req the Request
     * @param res the Response object
     * @return the string "success!"
     */
    private static String addToLikes(Request req, Response res) throws SQLException {
        Session session = getSession(req);
        GamesDatabase db = GamesDatabase.getInstance();

        Collection likes = session.attribute("likes");
        int id = Integer.parseInt(req.queryParams("id"));
        likes.addGame(db.getGameByID(id));

        return "success!";
    }

    /**
     * Removes a game with a given ID from the Likes collection.
     * @param req the Request
     * @param res the Response object
     * @return the string "success!"
     */
    private static String removeFromLikes(Request req, Response res) throws SQLException {
        Session session = getSession(req);
        GamesDatabase db = GamesDatabase.getInstance();

        Collection likes = session.attribute("likes");
        int id = Integer.parseInt(req.queryParams("id"));
        likes.removeGameByID(id);

        return "success!";
    }

    /**
     * Adds a game with a given ID to the Dislikes collection.
     * @param req the Request
     * @param res the Response object
     * @return the string "success!"
     */
    private static String addToDislikes(Request req, Response res) throws SQLException {
        Session session = getSession(req);
        GamesDatabase db = GamesDatabase.getInstance();

        Collection dislikes = session.attribute("dislikes");
        int id = Integer.parseInt(req.queryParams("id"));
        dislikes.addGame(db.getGameByID(id));

        return "success!";
    }

    /**
     * Removes a game with a given ID from the Dislikes collection.
     * @param req the Request
     * @param res the Response object
     * @return the string "success!"
     */
    private static String removeFromDislikes(Request req, Response res) throws SQLException {
        Session session = getSession(req);
        GamesDatabase db = GamesDatabase.getInstance();

        Collection dislikes = session.attribute("dislikes");
        int id = Integer.parseInt(req.queryParams("id"));
        dislikes.removeGameByID(id);

        return "success!";
    }

    /**
     * Sets up routes for the web server's routes and port
     *
     * @param serverPort the port to run the web server on
     */
    public static void run(int serverPort) {
        port(serverPort);
        staticFiles.location("/public");
        get("/", WebServer::serveMainTemplate);
        get("/remove_stew_item", WebServer::removeStewItem);
        get("/search", WebServer::searchGame);
        get("/add_stew_game", WebServer::addStewGame);
        get("/add_stew_property", WebServer::addStewProperty);
        get("/update_stew_filters", WebServer::updateStewFilters);
        get("/taste", WebServer::serveTastyStew);
        get("/collections", WebServer::serveCollectionsList);
        get("/create_collection", WebServer::createCollection);
        get("/delete_collection", WebServer::deleteCollection);
        get("/collection", WebServer::serveCollection);
        get("/add_to_collection", WebServer::addToCollection);
        get("/remove_from_collection", WebServer::removeFromCollection);
        get("/add_to_likes", WebServer::addToLikes);
        get("/remove_from_likes", WebServer::removeFromLikes);
        get("/add_to_dislikes", WebServer::addToDislikes);
        get("/remove_from_dislikes", WebServer::removeFromDislikes);
    }

    public static void main(String[] args) {
        run(1337);
    }
}
