package nro.models.map;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class MapLoopTest {
    private Map map() {
        return new Map(999, "test", (byte) 0, (byte) 1, (byte) 0, (byte) 0,
                (byte) 0, new int[][]{{0}}, new int[]{0}, 0, false, 20,
                Collections.emptyList(), Collections.emptyList());
    }

    @Test public void failedZoneDoesNotBusySpinOrStarveOtherZones() throws Exception {
        Map map = map();
        AtomicInteger failures = new AtomicInteger();
        CountDownLatch healthyTicks = new CountDownLatch(2);
        map.zones.add(new Zone(map, 0, 20) {
            @Override public void update() {
                failures.incrementAndGet();
                throw new IllegalStateException("broken zone fixture");
            }
        });
        map.zones.add(new Zone(map, 1, 20) {
            @Override public void update() { healthyTicks.countDown(); }
        });
        Thread thread = new Thread(map, "test-map-failure");
        thread.setDaemon(true);
        thread.start();
        try {
            assertTrue("another zone must still update", healthyTicks.await(4, TimeUnit.SECONDS));
            assertTrue("failed updates must wait for the next tick", failures.get() <= 4);
        } finally {
            thread.interrupt();
            thread.join(1000);
        }
        assertFalse("interrupt must stop the loop", thread.isAlive());
    }

    @Test public void sleepingMapStopsWhenInterrupted() throws Exception {
        Map map = map();
        CountDownLatch tick = new CountDownLatch(1);
        map.zones.add(new Zone(map, 0, 20) {
            @Override public void update() { tick.countDown(); }
        });
        Thread thread = new Thread(map, "test-map-stop");
        thread.setDaemon(true);
        thread.start();
        try {
            assertTrue(tick.await(1, TimeUnit.SECONDS));
        } finally {
            thread.interrupt();
            thread.join(1000);
        }
        assertFalse(thread.isAlive());
    }
}
