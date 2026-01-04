public class StewFilter {
    private FilterCategory category;
    private int value;

    StewFilter(FilterCategory category, int value) {
        this.category = category;
        this.value = value;
    }

    /**
     * Returns the SQL query part associated with this filter.
     * @return the query part as a String
     */
    public String getQuery() {
        return switch (category) {
            case FilterCategory.MIN_PLAYERS -> "minPlayers <= " + value;
            case FilterCategory.MAX_PLAYERS -> "maxPlayers >= " + value;
            case FilterCategory.MIN_AGE -> "minAge >= " + value;
            case FilterCategory.MIN_PLAYTIME -> "playTime >= " + value;
            case FilterCategory.MAX_PLAYTIME -> "playTime <= " + value;
            case FilterCategory.MIN_WEIGHT -> "weight >= " + value;
            case FilterCategory.MAX_WEIGHT -> "weight <= " + value;
            case FilterCategory.MIN_RATING -> "rating >= " + value;
            case FilterCategory.MAX_RATING -> "rating <= " + value;
            // TODO: Implement IN_COLLECTION filter!
            case FilterCategory.IN_COLLECTION -> "";
        };
    }
}