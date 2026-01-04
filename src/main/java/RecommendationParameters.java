import java.util.HashMap;

public class RecommendationParameters {
    /* general weights */
    public static float gameItemWeight = 1.0f;
    public static float propertyItemWeight = 1.0f;

    /* weights for game item comparison */
    public static float playerCountWeight = 1.0f;
    public static float playtimeWeight = 1.0f;
    public static float weightWeight = 1.0f;
    public static float propertiesWeight = 1000.0f;

    /* weights for property item comparison */
    public static float subTypeWeight = 0.25f;
    public static float valueWeight = 1.0f;

    /* weights for game property types */
    public static final HashMap<String, Float> propertyTypeWeights = new HashMap<>() {{
        put("designer", 0.25f);
        put("publisher", 0.0f);
        put("family", 1.0f);
        put("artist", 0.25f);
        put("category", 1.0f);
        put("mechanic", 1.0f);
    }};

    /**
     * Returns the weight assigned to a given property type, with default
     *
     * @param propertyType the property type as a string
     * @return the weight of the property type, 0 if no weight is specified
     */
    public static float getPropertyTypeWeight(String propertyType) {
        if (propertyTypeWeights.containsKey(propertyType)) {
            return propertyTypeWeights.get(propertyType);
        }
        return 0.0f;
    }
}
