package io.collective;

import java.time.Clock;

public class SimpleAgedCache {
    private Clock clock;
    int elements;
    ExpirabeEntry head;
    ExpirabeEntry tail;

    class ExpirabeEntry {
        Object key;
        Object value;
        long timeToExpiration;
        ExpirabeEntry next;
        ExpirabeEntry prev;

        ExpirabeEntry(Object k, Object v, int r) {
            key = k;
            value = v;
            timeToExpiration = clock.millis() + (long)r;
            next = null;
            prev = null;
        }

        boolean isExpired(Clock clock) {
            return clock.millis() > timeToExpiration;
        }
    }

    public SimpleAgedCache(Clock clock) {
        this();
        this.clock = clock;
    }

    public SimpleAgedCache() {
        head = null;
        tail = null;
        elements = 0;
        this.clock = Clock.systemUTC();
    }

    public void put(Object key, Object value, int retentionInMillis) {
        if (key == null || value == null) throw new NullPointerException("Key or value is null.");

        if (head == null) {
            head = new ExpirabeEntry(key, value, retentionInMillis);
            tail = head;
            elements++;
        } else {
            tail.next = new ExpirabeEntry(key, value, retentionInMillis);
            tail.next.prev = tail;
            tail = tail.next;
            elements++;
        }
    }

    public boolean isEmpty() {
        return (elements == 0);
    }

    public int size() {
        prune(head, clock);
        return elements;
    }

    public Object get(Object key) {
        if (head == null) { return null; }
        prune(head, clock);
        ExpirabeEntry node = head;

        while (node != null) {
            if (node.key == key) {
                return node.value;
            }
            node = node.next;
        }

        return null;
    }

    private void prune(ExpirabeEntry head, Clock clock) {
        ExpirabeEntry node_pointer = head;

        while (node_pointer != null) {
            if (node_pointer.isExpired(clock)) {
                if (node_pointer.equals(head)) {
                    head = node_pointer.next;
                    node_pointer.prev = null;
                } else {
                    node_pointer.prev.next = node_pointer.next;
                }
                if (node_pointer.equals(tail)) {
                    node_pointer.prev = null;
                } else {
                    node_pointer.next.prev = node_pointer.prev;
                }
                elements--;
            }
            node_pointer = node_pointer.next;
        }

        if (elements == 0) {
            head = null;
            tail = null;
        }
    }
}

