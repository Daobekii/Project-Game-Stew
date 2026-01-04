public class GameProperty {
    private int id;
    private String type;
    private String subtype;
    private String value;

    GameProperty(int id, String type, String subtype, String value){
        this.id = id;
        this.type = type;
        this.subtype = subtype;
        this.value = value;
    }

    public int getID() { return id; }
    public String getType() { return type; }
    public String getSubtype() { return subtype; }
    public String getValue() { return value; }

    /**
     * Returns the combined subtype (if any) and value. This is usually useful for displaying to the user.
     * @return the combined subtype-value
     */
    public String getCombinedValue() {
        if (this.subtype == null || this.subtype.isEmpty()) {
            return this.value;
        }
        return this.subtype + ": " + this.value;
    }
}
