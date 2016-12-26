package rocks.appconcept.javatools.collections;

import rocks.appconcept.javatools.CoverageTool;
import junit.framework.TestCase;

import java.util.Map;

/**
 * @author yanimetaxas
 */
public class MapsTest extends TestCase {

    public void testConstruction() throws Exception {

        CoverageTool.callPrivateConstructor(Maps.class);

        testMap(0, Maps.map());
        testMap(1, Maps.map("a1", "b1"));
        testMap(2, Maps.map("a1", "b1", "a2", "b2"));
        testMap(3, Maps.map("a1", "b1", "a2", "b2", "a3", "b3"));
        testMap(4, Maps.map("a1", "b1", "a2", "b2", "a3", "b3", "a4", "b4"));
        testMap(5, Maps.map("a1", "b1", "a2", "b2", "a3", "b3", "a4", "b4", "a5", "b5"));
        testMap(6, Maps.map("a1", "b1", "a2", "b2", "a3", "b3", "a4", "b4", "a5", "b5", "a6", "b6"));
        testMap(7, Maps.map("a1", "b1", "a2", "b2", "a3", "b3", "a4", "b4", "a5", "b5", "a6", "b6", "a7", "b7"));
        testMap(8, Maps.map("a1", "b1", "a2", "b2", "a3", "b3", "a4", "b4", "a5", "b5", "a6", "b6", "a7", "b7", "a8", "b8"));
        testMap(9, Maps.map("a1", "b1", "a2", "b2", "a3", "b3", "a4", "b4", "a5", "b5", "a6", "b6", "a7", "b7", "a8", "b8", "a9", "b9"));
        testMap(10, Maps.map("a1", "b1", "a2", "b2", "a3", "b3", "a4", "b4", "a5", "b5", "a6", "b6", "a7", "b7", "a8", "b8", "a9", "b9", "a10", "b10"));

        testMap2(1, Maps.map("a1", 1));
        testMap2(2, Maps.map("a1", 1, "a2", 2));
        testMap2(3, Maps.map("a1", 1, "a2", 2, "a3", 3));
        testMap2(4, Maps.map("a1", 1, "a2", 2, "a3", 3, "a4", 4));
        testMap2(5, Maps.map("a1", 1, "a2", 2, "a3", 3, "a4", 4, "a5", 5));
        testMap2(6, Maps.map("a1", 1, "a2", 2, "a3", 3, "a4", 4, "a5", 5, "a6", 6));
        testMap2(7, Maps.map("a1", 1, "a2", 2, "a3", 3, "a4", 4, "a5", 5, "a6", 6, "a7", 7));
        testMap2(8, Maps.map("a1", 1, "a2", 2, "a3", 3, "a4", 4, "a5", 5, "a6", 6, "a7", 7, "a8", 8));
        testMap2(9, Maps.map("a1", 1, "a2", 2, "a3", 3, "a4", 4, "a5", 5, "a6", 6, "a7", 7, "a8", 8, "a9", 9));
        testMap2(10, Maps.map("a1", 1, "a2", 2, "a3", 3, "a4", 4, "a5", 5, "a6", 6, "a7", 7, "a8", 8, "a9", 9, "a10", 10));
    }

    private void testMap(int counter, Map<String, String> map) throws Exception {
        assertEquals(counter, map.size());
        for (int i=1; i<=counter; i++) {
            assertEquals("b" + i, map.get("a" + i));
        }
    }

    private void testMap2(int counter, Map<String, Integer> map) throws Exception {
        assertEquals(counter, map.size());
        for (int i=1; i<=counter; i++) {
            assertEquals(i, (int) map.get("a" + i));
        }
    }
}