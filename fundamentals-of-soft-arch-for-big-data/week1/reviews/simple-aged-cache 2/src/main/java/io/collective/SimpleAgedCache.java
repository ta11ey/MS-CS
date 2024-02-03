package io.collective;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SimpleAgedCache {

    private final Map<Object, CacheEntry> cache = new ConcurrentHashMap<>();
    private final Clock clock;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public SimpleAgedCache(Clock clock) {
        this.clock = clock;
    }

    public SimpleAgedCache() {
        this(Clock.systemDefaultZone());
    }

    public void put(Object key, Object value, int retentionInMillis) {
        Instant expirationTime = Instant.now(clock).plusMillis(retentionInMillis);
        cache.put(key, new CacheEntry(value, expirationTime));
    }

    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            removeExpiredEntries();
            return cache.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    public int size() {
        lock.readLock().lock();
        try {
            removeExpiredEntries();
            return cache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Object get(Object key) {
        lock.readLock().lock();
        try {
            removeExpiredEntries();

            CacheEntry entry = cache.get(key);
            if (entry != null && !entry.isExpired(clock.instant())) {
                return entry.getValue();
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    private void removeExpiredEntries() {
        Instant currentTime = clock.instant();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired(currentTime));
    }

    private static class CacheEntry {
        private final Object value;
        private final Instant expirationTime;

        CacheEntry(Object value, Instant expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }

        Object getValue() {
            return value;
        }

        boolean isExpired(Instant currentTime) {
            return currentTime.isAfter(expirationTime);
        }
    }
}
