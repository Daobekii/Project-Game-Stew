import java.util.ArrayList;
public class Program {
    private ArrayList<Collection> collections = new ArrayList<Collection>();
    private Profile profile;
    private Stew stew = new Stew();

    public Program(ArrayList<Collection> collections, Profile profile) {
        this.collections = collections;
        this.profile = profile;
    }

    /**
     * Adds a new collection.
     *
     * @param role the special role (e.g. NONE, LIKES, DISLIKES) associated with the collection
     * @param name the name of the collection
     * @param icon the icon of the collection
     */
    public void addCollection(SpecialRole role, String name, String icon) {
        collections.add(new Collection(collections.size(), role, name, icon));
    }

    /**
     * Removes a collection by its ID.
     * Iterates through the list of collections to find the one with the matching ID and removes it.
     *
     * @param id the ID of the collection to remove
     */
    public void removeCollection(int id) {
        for (int i = 0; i < collections.size(); i++) {
            if (collections.get(i).getID() == id) {
                collections.remove(i);
                break;
            }
        }
    }
    /**
     * Searches for games that match the query within the collections/database.
     * This method is currently not implemented.
     *
     * @param query the search query
     */
    public void searchGames(String query) {
        //TODO
    }
}
