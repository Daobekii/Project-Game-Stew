public class Commonality {
    private StewItem stewItem;
    private String type;
    private String name;
    private String url;
    private int associatedID; // this is the ID associated not with the Commonality, but with the property, if any

    Commonality(StewItem item, String type, String name, String url, int associatedID) {
        this.stewItem = item;
        this.type = type;
        this.name = name;
        this.url = url;
        this.associatedID = associatedID;
    }

    public StewItem getItem() { return stewItem; }
    public String getUrl() { return url; }
    public String getType() { return type; }
    public String getName() { return name; }
    public int getAssociatedID() { return associatedID; }
}
