package nro.server.security;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.LongSupplier;

/**
 * Lightweight application-layer admission control for the raw TCP game port.
 *
 * This protects JVM file descriptors/threads from connection churn. It is NOT
 * a substitute for upstream protection against volumetric TCP/UDP DDoS.
 * All counters are bounded in memory. No user credentials or packet data
 * are logged or stored here.
 */
public final class ConnectionGuard {

    private static final long TEN_SECONDS = 10_000L;
    private static final long ONE_MINUTE = 60_000L;
    private static final long IP_IDLE_EXPIRY = 120_000L;
    private static final long BLOCK_DURATION = 30_000L;
    private static final int MAX_TRACKED_IPS = 20_000;

    private final LongSupplier clock;
    private final int perIpTenSeconds;
    private final int perIpMinute;
    private final int globalPerSecond;
    private final int maxActiveGlobal;
    private final Map<String, IpState> ipStates = new HashMap<>();

    private int activeGlobal;
    private long globalWindow;
    private int globalAccepted;
    private long lastSweep;
    private long rejectedSinceReport;
    private long lastReport;

    private static final class IpState {
        long window10;
        long window60;
        long lastSeen;
        long bannedUntil;
        int accepted10;
        int accepted60;
        int rejected10;
        int active;
    }

    private static int setting(String name, int fallback) {
        return Math.max(1, Integer.getInteger(name, fallback));
    }

    public ConnectionGuard() {
        this(System::currentTimeMillis,
                setting("nro.guard.connectionsPer10SecPerIp", 20),
                setting("nro.guard.connectionsPerMinutePerIp", 60),
                setting("nro.guard.newConnectionsPerSecondGlobal", 150),
                setting("nro.guard.maxActiveConnectionsGlobal", 1400));
    }

    // Package-visible for deterministic, real-clock-free tests.
    ConnectionGuard(LongSupplier clock, int tenSec, int minute, int globalSec, int activeGlobalLimit) {
        this.clock = clock;
        this.perIpTenSeconds = Math.max(1, tenSec);
        this.perIpMinute = Math.max(1, minute);
        this.globalPerSecond = Math.max(1, globalSec);
        this.maxActiveGlobal = Math.max(1, activeGlobalLimit);
    }

    public synchronized boolean tryAcquire(String ip, int perIpActiveLimit) {
        long now = clock.getAsLong();
        if (now - lastSweep >= ONE_MINUTE || now < lastSweep) {
            sweep(now);
        }
        if (now < globalWindow || now - globalWindow >= 1_000L) {
            globalWindow = now;
            globalAccepted = 0;
        }
        if (activeGlobal >= maxActiveGlobal || globalAccepted >= globalPerSecond) {
            return reject(null, now);
        }

        IpState state = ipStates.get(ip);
        if (state == null) {
            if (ipStates.size() >= MAX_TRACKED_IPS) {
                return reject(null, now);
            }
            state = new IpState();
            state.window10 = now;
            state.window60 = now;
            ipStates.put(ip, state);
        }
        state.lastSeen = now;
        if (now < state.window10 || now - state.window10 >= TEN_SECONDS) {
            state.window10 = now;
            state.accepted10 = 0;
            state.rejected10 = 0;
        }
        if (now < state.window60 || now - state.window60 >= ONE_MINUTE) {
            state.window60 = now;
            state.accepted60 = 0;
        }

        if (now < state.bannedUntil || state.active >= Math.max(1, perIpActiveLimit)
                || state.accepted10 >= perIpTenSeconds || state.accepted60 >= perIpMinute) {
            return reject(state, now);
        }
        state.active++;
        activeGlobal++;
        state.accepted10++;
        state.accepted60++;
        globalAccepted++;
        return true;
    }

    private boolean reject(IpState state, long now) {
        rejectedSinceReport++;
        if (state != null && now >= state.bannedUntil && ++state.rejected10 >= 30) {
            state.bannedUntil = now + BLOCK_DURATION;
        }
        return false;
    }

    public synchronized void release(String ip) {
        IpState state = ipStates.get(ip);
        if (state != null && state.active > 0) {
            state.active--;
            activeGlobal--;
            state.lastSeen = clock.getAsLong();
        }
    }

    public synchronized int getActiveGlobal() {
        return activeGlobal;
    }

    public synchronized int getActiveForIp(String ip) {
        IpState state = ipStates.get(ip);
        return state == null ? 0 : state.active;
    }

    /** Log no more than once in 15 seconds, regardless of flood size. */
    public synchronized long takeRejectionsForLog() {
        long now = clock.getAsLong();
        if (now - lastReport < 15_000L) {
            return 0;
        }
        lastReport = now;
        long count = rejectedSinceReport;
        rejectedSinceReport = 0;
        return count;
    }

    private void sweep(long now) {
        lastSweep = now;
        Iterator<Map.Entry<String, IpState>> iterator = ipStates.entrySet().iterator();
        while (iterator.hasNext()) {
            IpState state = iterator.next().getValue();
            if (state.active == 0 && now - state.lastSeen >= IP_IDLE_EXPIRY) {
                iterator.remove();
            }
        }
    }
}
