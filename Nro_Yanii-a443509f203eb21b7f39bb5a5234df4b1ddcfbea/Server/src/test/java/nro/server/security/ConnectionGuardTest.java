package nro.server.security;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConnectionGuardTest {

    @Test
    public void concurrentSlotsAreReleasedOnDisconnect() {
        AtomicLong time = new AtomicLong(100_000);
        ConnectionGuard guard = new ConnectionGuard(time::get, 20, 60, 150, 100);
        assertTrue(guard.tryAcquire("203.0.113.10", 2));
        assertTrue(guard.tryAcquire("203.0.113.10", 2));
        assertFalse(guard.tryAcquire("203.0.113.10", 2));
        assertEquals(2, guard.getActiveForIp("203.0.113.10"));
        guard.release("203.0.113.10");
        assertTrue(guard.tryAcquire("203.0.113.10", 2));
        guard.release("203.0.113.10");
        guard.release("203.0.113.10");
        assertEquals(0, guard.getActiveGlobal());
    }

    @Test
    public void tenNewConnectionsInTenSecondsPerIp() {
        AtomicLong time = new AtomicLong(100_000);
        ConnectionGuard guard = new ConnectionGuard(time::get, 10, 60, 150, 1400);
        for (int i = 0; i < 10; ++i) {
            assertTrue("connection " + i, guard.tryAcquire("203.0.113.10", 5));
            guard.release("203.0.113.10");
        }
        assertFalse("eleventh connection must be rejected", guard.tryAcquire("203.0.113.10", 5));
        // A different IP remains independently eligible, but both still use
        // the same global acceptance budget.
        assertTrue(guard.tryAcquire("203.0.113.11", 5));
        time.addAndGet(10_001);
        assertTrue(guard.tryAcquire("203.0.113.10", 5));
    }

    @Test
    public void perIpWindowRejectsConnectChurnAndRecovers() {
        AtomicLong time = new AtomicLong(100_000);
        ConnectionGuard guard = new ConnectionGuard(time::get, 2, 60, 150, 100);
        assertTrue(guard.tryAcquire("203.0.113.10", 5));
        guard.release("203.0.113.10");
        assertTrue(guard.tryAcquire("203.0.113.10", 5));
        guard.release("203.0.113.10");
        assertFalse(guard.tryAcquire("203.0.113.10", 5));
        time.addAndGet(10_001);
        assertTrue(guard.tryAcquire("203.0.113.10", 5));
    }

    @Test
    public void perIpMinuteLimitStillAppliesAcrossTenSecondWindows() {
        AtomicLong time = new AtomicLong(100_000);
        ConnectionGuard guard = new ConnectionGuard(time::get, 20, 2, 150, 100);
        for (int i = 0; i < 2; ++i) {
            assertTrue(guard.tryAcquire("203.0.113.10", 5));
            guard.release("203.0.113.10");
        }
        time.addAndGet(11_000);
        assertFalse(guard.tryAcquire("203.0.113.10", 5));
        time.addAndGet(49_001);
        assertTrue(guard.tryAcquire("203.0.113.10", 5));
    }

    @Test
    public void globalLimitsPreventTooManyNewSockets() {
        AtomicLong time = new AtomicLong(100_000);
        ConnectionGuard guard = new ConnectionGuard(time::get, 20, 60, 2, 2);
        assertTrue(guard.tryAcquire("203.0.113.1", 5));
        assertTrue(guard.tryAcquire("203.0.113.2", 5));
        assertFalse(guard.tryAcquire("203.0.113.3", 5)); // active cap
        guard.release("203.0.113.1");
        assertFalse(guard.tryAcquire("203.0.113.3", 5)); // global/sec cap
        time.addAndGet(1_001);
        assertTrue(guard.tryAcquire("203.0.113.3", 5));
    }

    @Test
    public void repeatedAuthenticationCannotFloodTheLoginDatabase() {
        AtomicLong time = new AtomicLong(100_000);
        ConnectionGuard guard = new ConnectionGuard(time::get, 100, 100, 200, 100);
        assertTrue(guard.tryAcquire("203.0.113.10", 5));
        for (int i = 0; i < 15; ++i) {
            assertTrue(guard.allowAuthentication("203.0.113.10"));
        }
        assertFalse(guard.allowAuthentication("203.0.113.10"));
        time.addAndGet(10_001);
        assertTrue(guard.allowAuthentication("203.0.113.10"));
    }

    @Test
    public void repeatedRejectionsTriggerTemporaryCooldown() {
        AtomicLong time = new AtomicLong(100_000);
        ConnectionGuard guard = new ConnectionGuard(time::get, 100, 100, 200, 100);
        assertTrue(guard.tryAcquire("203.0.113.10", 1));
        for (int i = 0; i < 30; ++i) {
            assertFalse(guard.tryAcquire("203.0.113.10", 1));
        }
        guard.release("203.0.113.10");
        assertFalse(guard.tryAcquire("203.0.113.10", 1));
        time.addAndGet(30_001);
        assertTrue(guard.tryAcquire("203.0.113.10", 1));
    }
}
