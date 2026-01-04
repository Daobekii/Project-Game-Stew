import java.util.ArrayList;

public class Profile {
    private ArrayList<GameProperty> allergy = new ArrayList<GameProperty>();
    private String name;
    private int age;

    public Profile(ArrayList<GameProperty> allergy, String name, int age) {
        this.allergy = allergy;
        this.name = name;
        this.age = age;
    }

    /**
     * Adds a GameProperty to the list of allergies, causing it to be excluded
     * from future recommendations.
     *
     * @param property the GameProperty
     */
    public void addAllergy(GameProperty property) {
        allergy.add(property);
    }
}
