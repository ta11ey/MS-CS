package io.collective;

import java.time.Clock;
import java.time.Instant;

public class SimpleAgedCache {
    private final Clock clock;
    private ExpirableEntry[] entries;
    private int count;

    public SimpleAgedCache(Clock clock) {
        this.clock = clock;
        this.entries = new ExpirableEntry[10]; // Initial capacity
        this.count = 0;
    }

    public SimpleAgedCache() {
        this(Clock.systemDefaultZone());
    }

    public void put(Object key, Object value, int retentionInMillis) {
        cleanUpExpiredEntries();
        if (count == entries.length) {
            resizeEntriesArray();
        }
        entries[count++] = new ExpirableEntry(key, value, retentionInMillis);
    }

    public boolean isEmpty() {
        cleanUpExpiredEntries();
        return count == 0;
    }

    public int size() {
        cleanUpExpiredEntries();
        return count;
    }

    public Object get(Object key) {
        cleanUpExpiredEntries();
        for (int i = 0; i < count; i++) {
            ExpirableEntry entry = entries[i];
            if (entry.key.equals(key) && !entry.isExpired()) {
                return entry.value;
            }
        }
        return null;
    }

    private void cleanUpExpiredEntries() {
        int shift = 0;
        for (int i = 0; i < count; i++) {
            if (entries[i].isExpired()) {
                shift++;
            } else if (shift > 0) {
                entries[i - shift] = entries[i];
            }
        }
        count -= shift;
    }

    private void resizeEntriesArray() {
        ExpirableEntry[] newEntries = new ExpirableEntry[entries.length * 2];
        System.arraycopy(entries, 0, newEntries, 0, count);
        entries = newEntries;
    }

    private class ExpirableEntry {
        Object key;
        Object value;
        Instant expiryTime;

        ExpirableEntry(Object key, Object value, int retentionInMillis) {
            this.key = key;
            this.value = value;
            this.expiryTime = clock.instant().plusMillis(retentionInMillis);
        }

        boolean isExpired() {
            return Instant.now(clock).isAfter(expiryTime);
        }
    }
}
