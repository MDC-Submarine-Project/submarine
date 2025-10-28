package dev.senj.common;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

// listens for any changes in the object to write back to other side
public class ObjectListener {

    // Mapping from a source object (by hash) to its destination counterpart
    private final ConcurrentHashMap<DestinationMapping, Object> objectMapping = new ConcurrentHashMap<>();
    // Last known state signature per mapping to detect changes across checks
    private final ConcurrentHashMap<DestinationMapping, Integer> lastStateSignatures = new ConcurrentHashMap<>();

    private volatile boolean running = false;
    private Thread listenerThread;

    // source to destination object hash
    public static class DestinationMapping implements Comparable<DestinationMapping> {
        int sourceHash;
        int destinationHash;

        public DestinationMapping(int sourceHash, int destinationHash) {
            this.sourceHash = sourceHash;
            this.destinationHash = destinationHash;
        }

        @Override
        public int compareTo(DestinationMapping other) {
            return Integer.compare(this.sourceHash, other.sourceHash);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DestinationMapping that = (DestinationMapping) o;
            return sourceHash == that.sourceHash;
        }

        @Override
        public int hashCode() {
            return sourceHash;
        }
    }

    /**
     * Callback when an object's observed state changed since the last check.
     * Override or replace usage as needed by the caller.
     */
    public void onObjectChanged(DestinationMapping mapping, Object value) {
        // Minimal default behavior: print a trace line. Replace with real propagation.
        System.out.println("[ObjectListener] Change detected for mapping src=" + mapping.sourceHash + " -> dst=" + mapping.destinationHash);
    }

    // Kept for backward compatibility; calls the new overload without details
    public void onObjectChanged() {
        // No-op default
    }

    /**
     * Starts a background thread that periodically checks for changes in the registered objects' observable state.
     * If already started, this method is a no-op.
     */
    public synchronized void start() {
        if (running) return;
        running = true;
        listenerThread = new Thread(() -> {
            while (running) {
                try {
                    // Iterate over current mappings and detect state changes compared to the last check
                    for (var entry : objectMapping.entrySet()) {
                        DestinationMapping key = entry.getKey();
                        Object value = entry.getValue();
                        int signature = computeStateSignature(value);

                        Integer lastSig = lastStateSignatures.put(key, signature);
                        if (lastSig != null && lastSig != signature) {
                            // A change was detected for this specific object since the previous check
                            onObjectChanged(key, value);
                            // Also call legacy hook in case external code relies on it
                            onObjectChanged();
                        }
                    }
                    // Brief sleep to avoid busy-waiting; adjust as needed
                    Thread.sleep(16); // ~60 Hz
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Throwable t) {
                    // Prevent the loop from dying due to unexpected exceptions
                    t.printStackTrace();
                }
            }
        }, "ObjectListener-ChangeWatcher");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Stops the background change watcher thread if it is running.
     */
    public synchronized void stop() {
        running = false;
        if (listenerThread != null) {
            listenerThread.interrupt();
            listenerThread = null;
        }
    }

    /**
     * Registers or updates an object under the given mapping.
     */
    public void put(DestinationMapping mapping, Object obj) {
        objectMapping.put(mapping, obj);
        // Initialize its signature so that the first detection occurs on actual change
        lastStateSignatures.put(mapping, computeStateSignature(obj));
    }

    /**
     * Utility to compute a stable signature that reflects the object's observed state.
     * Signature is computed from the object's bytes (CRC32), when possible.
     * Falls back gracefully if bytes cannot be obtained.
     */
    private int computeStateSignature(Object obj) {
        try {
            byte[] data = toBytes(obj);
            if (data != null) {
                CRC32 crc = new CRC32();
                crc.update(data);
                long v = crc.getValue();
                return (int) v; // CRC32 is 32-bit; narrowing is fine
            }
        } catch (Throwable ignored) {
            // fall through to fallback
        }
        // Fallback: use object's hashCode (may not reflect mutations if hashCode is constant)
        return obj == null ? 0 : obj.hashCode();
    }

    private byte[] toBytes(Object obj) throws IOException {
        if (obj == null) return new byte[0];
        // Specialized fast paths
        if (obj instanceof byte[] arr) {
            return arr;
        }
        if (obj instanceof ByteBuffer buf) {
            ByteBuffer dup = buf.asReadOnlyBuffer();
            byte[] out = new byte[dup.remaining()];
            dup.get(out);
            return out;
        }
        if (obj instanceof CharSequence cs) {
            return cs.toString().getBytes(StandardCharsets.UTF_8);
        }
        // Try Java serialization (only works if object implements Serializable)
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            oos.flush();
            return baos.toByteArray();
        } catch (IOException serEx) {
            // Not serializable or failed: fallback to toString bytes
            String s = obj.toString();
            return s != null ? s.getBytes(StandardCharsets.UTF_8) : new byte[0];
        }
    }

    private static final ObjectListener INSTANCE = new ObjectListener();

    public static ObjectListener getInstance() {
        return INSTANCE;
    }
}
