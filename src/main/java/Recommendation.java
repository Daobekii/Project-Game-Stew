import java.util.List;

public class Recommendation {
    private Game game;
    private List<Commonality> commonalities;

    public Recommendation(Game game, List<Commonality> commonalities) {
        this.game = game;
        this.commonalities = commonalities;
    }

    public Game getGame() { return game; }
    public List<Commonality> getCommonalities() { return commonalities; }
}