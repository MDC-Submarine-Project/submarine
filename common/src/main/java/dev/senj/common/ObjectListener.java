package dev.senj.common;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

// listens for any changes in the object to write back to other side
public class ObjectListener {

    // Mapping from a source object (by hash) to its destination counterpart
    private final ConcurrentHashMap<DestinationMapping, Object> objectMapping = new ConcurrentHashMap<>();
    // Fast lookup by mapping id
    private final ConcurrentHashMap<String, DestinationMapping> idIndex = new ConcurrentHashMap<>();
    // Last known state signature per mapping to detect changes across checks
    private final ConcurrentHashMap<DestinationMapping, Integer> lastStateSignatures = new ConcurrentHashMap<>();

    private volatile boolean running = false;
    private Thread listenerThread;

    // Network-related fields
    private NetworkManager networkManager;
    private boolean isNetworkInitialized = false;

    // source to destination object hash
    public static class DestinationMapping {
        private final String id;

        public DestinationMapping(String id) {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("DestinationMapping id must not be null or blank");
            }
            this.id = id;
        }

        public String getId() { return id; }

        @Override
        public String toString() {
            return "DestinationMapping{" +
                    "id='" + id + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DestinationMapping that = (DestinationMapping) o;
            return id.equals(that.id);
        }

        @Override
        public int hashCode() { return id.hashCode(); }
    }

    /**
     * Callback when an object's observed state changed since the last check.
     * Sends the object over the network if network is initialized.
     */
    public void onObjectChanged(DestinationMapping mapping, Object value) {
        System.out.println("[ObjectListener] Change detected for mapping id=" + mapping.getId() + ", type=" + value.getClass());

        if (isNetworkInitialized) {
            try {
                // Convert the object to bytes (payload)
                byte[] payload = toBytes(value);

                // Compute CRC32 as source hash of payload
                CRC32 crc = new CRC32();
                crc.update(payload);
                int crc32 = (int) crc.getValue();

                // Frame structure: [int idLen][id bytes UTF-8][int crc32][int payloadLen][payload]
                byte[] idBytes = mapping.getId().getBytes(StandardCharsets.UTF_8);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);
                dos.writeInt(idBytes.length);
                dos.write(idBytes);
                dos.writeInt(crc32);
                dos.writeInt(payload.length);
                dos.write(payload);
                dos.flush();

                networkManager.transmit(baos.toByteArray());
                System.out.println("[ObjectListener] Sent object over network with id=" + mapping.getId() + ": " + value);
            } catch (IOException e) {
                System.err.println("[ObjectListener] Error sending object over network: " + e.getMessage());
            }
        }
    }

    /**
     * Initializes the network as a server.
     * @param port The port to listen on
     * @throws IOException If an I/O error occurs
     */
    public void initializeAsServer(int port) throws IOException {
        if (isNetworkInitialized) return;

        networkManager = NetworkManager.getInstance();
        networkManager.startServer(port, this::handleReceivedData);
        isNetworkInitialized = true;
        System.out.println("[ObjectListener] Initialized as server on port " + port);
    }

    /**
     * Initializes the network as a client.
     * @param host The server host
     * @param port The server port
     * @throws IOException If an I/O error occurs
     */
    public void initializeAsClient(String host, int port) throws IOException {
        if (isNetworkInitialized) return;

        networkManager = NetworkManager.getInstance();
        networkManager.connectToServer(host, port, this::handleReceivedData);
        isNetworkInitialized = true;
        System.out.println("[ObjectListener] Initialized as client connecting to " + host + ":" + port);
    }

    /**
     * Handles data received from the network.
     * Frame: [int idLen][id bytes UTF-8][int crc32][int payloadLen][payload]
     * @param data The received data
     */
    private void handleReceivedData(byte[] data) {
        try {
            if (data == null || data.length < 16) return; // minimal header

            try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                 DataInputStream dis = new DataInputStream(bais)) {
                int idLen = dis.readInt();
                if (idLen < 0 || idLen > 65535) throw new IOException("Invalid id length: " + idLen);
                byte[] idBytes = new byte[idLen];
                dis.readFully(idBytes);
                String id = new String(idBytes, StandardCharsets.UTF_8);

                int crc32 = dis.readInt();
                int payloadLen = dis.readInt();
                if (payloadLen < 0) throw new IOException("Invalid payload length: " + payloadLen);
                byte[] payload = new byte[payloadLen];
                dis.readFully(payload);

                Object obj = fromBytes(payload);
                if (obj == null) return;

                System.out.println("[ObjectListener] Received object id=" + id + ", payloadType=" + obj.getClass());

                DestinationMapping mapping = idIndex.get(id);
                if (mapping == null) {
                    System.out.println("[ObjectListener] No local mapping registered for id=" + id + ". Ignoring.");
                    return;
                }
                Object localObj = objectMapping.get(mapping);
                if (localObj == null) {
                    System.out.println("[ObjectListener] Mapping id=" + id + " has no local object. Ignoring.");
                    return;
                }

                if (localObj.getClass().equals(obj.getClass())) {
                    if (copyFields(obj, localObj)) {
                        System.out.println("[ObjectListener] Updated local object for id=" + id + ": " + localObj);
                    }
                    // Use the received CRC as the new signature to avoid echo
                    lastStateSignatures.put(mapping, crc32);
                } else {
                    System.err.println("[ObjectListener] Type mismatch for id=" + id + ": local=" + localObj.getClass() + ", incoming=" + obj.getClass());
                }
            }
        } catch (Exception e) {
            System.err.println("[ObjectListener] Error processing received data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a new instance of the given class using ASM if necessary.
     * @param clazz The class to create an instance of
     * @return A new instance of the class
     */
    private Object createInstance(Class<?> clazz) throws Exception {
        return clazz.getDeclaredConstructor().newInstance();
    }

    /**
     * Copies all fields from source object to destination object.
     * @param source The source object
     * @param destination The destination object
     * @return true if fields were copied successfully, false otherwise
     */
    private boolean copyFields(Object source, Object destination) {
        if (source == null || destination == null || !source.getClass().equals(destination.getClass())) {
            return false;
        }

        try {
            // Generic approach for all objects using reflection
            for (Field field : source.getClass().getDeclaredFields()) {
                // Skip static and final fields
                int modifiers = field.getModifiers();
                if (java.lang.reflect.Modifier.isStatic(modifiers) || java.lang.reflect.Modifier.isFinal(modifiers)) {
                    continue;
                }

                field.setAccessible(true);
                Object value = field.get(source);
                field.set(destination, value);
            }
            return true;
        } catch (Exception e) {
            System.err.println("[ObjectListener] Error copying fields: " + e.getMessage());
            return false;
        }
    }

    /**
     * Converts bytes back to an object.
     * @param data The bytes to convert
     * @return The deserialized object
     */
    private Object fromBytes(byte[] data) throws IOException, ClassNotFoundException {
        if (data == null || data.length == 0) return null;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        }
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
     * Stops the background change watcher thread and network connections if they are running.
     */
    public synchronized void stop() {
        running = false;
        if (listenerThread != null) {
            listenerThread.interrupt();
            listenerThread = null;
        }

        // Stop the network manager if it's initialized
        if (isNetworkInitialized && networkManager != null) {
            networkManager.stop();
            isNetworkInitialized = false;
        }
    }

    /**
     * Registers or updates an object under the given mapping.
     */
    public void put(DestinationMapping mapping, Object obj) {
        objectMapping.put(mapping, obj);
        idIndex.put(mapping.getId(), mapping);
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
