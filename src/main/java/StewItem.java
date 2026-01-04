import java.util.List;

public interface StewItem {
     
    float getScore(Game target);
    float getWeight();

    float getSize();
    void setSize(float size);

    String getImageUrl();
    void setImageUrl(String imageUrl);

    String getName();
    void setName(String name);

    int getID();
    void setID(int id);

    List<Commonality> getCommonalities(Game target);
}
