import java.util.ArrayList;
import java.util.List;

public class PropertyItem implements StewItem{

    private int id;
    private GameProperty property;

    public PropertyItem(int id, GameProperty property){
        this.id = id;
        this.property = property;
    }

    /**
     * Calculates and returns the degree of similarity between the target Game and this item.
     * The similarity score is based on the fraction of matching relevant properties
     * between the target Game and the property of this item.
     * 
     * @param target the Game to compare with
     * @return the similarity score
     */
    @Override
    public float getScore(Game target) {
        float score = 0;

        for (GameProperty targetProperty : target.getProperties()) {
            if(targetProperty.getID() == property.getID()){
                score = RecommendationParameters.valueWeight;
                break;
            }
            if(targetProperty.getSubtype() != null && targetProperty.getSubtype().equals(property.getSubtype())) {
                score = RecommendationParameters.subTypeWeight;
            }
        }

        return RecommendationParameters.propertyItemWeight * score;
    }

    /**
     * Returns a list of all commonalities of the target game with this item
     * @param target the target game
     * @return the list of commonalities
     */
    @Override
    public List<Commonality> getCommonalities(Game target) {
        ArrayList<Commonality> commonalities = new ArrayList<>();
        for (GameProperty targetProperty : target.getProperties()) {
            if(targetProperty.getID() == property.getID()){
                commonalities.add(new Commonality(
                    this,
                    "Same " + property.getType(),
                    property.getCombinedValue(),
                    "",
                    property.getID()
                ));
            }
        }
        return commonalities;
    }

    /**
     * @return the weight of the item
     */
    @Override
    public float getWeight() {
        return RecommendationParameters.propertyItemWeight;
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
        return "/placeholder.png";
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
        return property.getCombinedValue();
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
        return this.id;
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
