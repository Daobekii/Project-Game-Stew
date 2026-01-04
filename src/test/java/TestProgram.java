import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

public class TestProgram {
    private Program program;
    private ArrayList<Collection> collections;

    @BeforeEach
    public void setUp() {
        collections = new ArrayList<>();
        program = new Program(collections, new Profile(new ArrayList<>(), "User", 25));
    }

    @Test
    public void testRemoveCollection_ValidId() {
        Collection coll1 = new Collection(1, SpecialRole.NONE, "Collection 1", "Icon1");
        Collection coll2 = new Collection(2, SpecialRole.LIKES, "Collection 2", "Icon2");
        collections.add(coll1);
        collections.add(coll2);

        program.removeCollection(1);
        assertFalse(collections.contains(coll1), "Collection with ID 1 should be removed");
        assertEquals(1, collections.size(), "One collection should remain");
    }

    @Test
    public void testRemoveCollection_InvalidId() {
        Collection coll1 = new Collection(1, SpecialRole.NONE, "Collection 1", "Icon1");
        collections.add(coll1);

        program.removeCollection(2);  // Attempting to remove a non-existing ID
        assertEquals(1, collections.size(), "No collections should be removed");
    }

    @Test
    public void testRemoveCollection_EmptyList() {
        program.removeCollection(1);  // Trying to remove from an empty list
        assertTrue(collections.isEmpty(), "List should remain empty");
    }
}
