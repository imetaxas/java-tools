package rocks.appconcept.javatools.profiler;

import rocks.appconcept.javatools.CoverageTool;
import junit.framework.TestCase;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yanimetaxas
 */
public class ProfilerTest extends TestCase {

    public void testBasicOperation() throws Exception {

        CoverageTool.callPrivateConstructor(Profiler.class);

        AtomicInteger counter = new AtomicInteger(0);
        String profile = Profiler.sampling(10, () -> {
            Thread.sleep(10);
            counter.incrementAndGet();
            Thread.sleep(10);
        });

        assertEquals(10, counter.get());
        assertNotNull(profile);

    }
}