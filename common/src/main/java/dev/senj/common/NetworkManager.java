package dev.senj.common;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Handles network communication between client and server.
 * Provides methods for sending and receiving objects over TCP.
 */
public class NetworkManager {
    private static final NetworkManager INSTANCE = new NetworkManager();
    
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private ExecutorService executorService;
    private boolean isRunning = false;
    private Consumer<byte[]> dataReceiver;
    
    private NetworkManager() {
        executorService = Executors.newCachedThreadPool();
    }
    
    public static NetworkManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Starts a server that listens for incoming connections.
     * @param port The port to listen on
     * @param dataReceiver A consumer that processes received data
     * @throws IOException If an I/O error occurs
     */
    public void startServer(int port, Consumer<byte[]> dataReceiver) throws IOException {
        if (isRunning) return;
        
        this.dataReceiver = dataReceiver;
        serverSocket = new ServerSocket(port);
        isRunning = true;
        
        executorService.submit(() -> {
            try {
                System.out.println("Server started on port " + port);
                clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                
                outputStream = new DataOutputStream(clientSocket.getOutputStream());
                inputStream = new DataInputStream(clientSocket.getInputStream());
                
                startReceiving();
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Connects to a server.
     * @param host The server host
     * @param port The server port
     * @param dataReceiver A consumer that processes received data
     * @throws IOException If an I/O error occurs
     */
    public void connectToServer(String host, int port, Consumer<byte[]> dataReceiver) throws IOException {
        if (isRunning) return;
        
        this.dataReceiver = dataReceiver;
        clientSocket = new Socket(host, port);
        outputStream = new DataOutputStream(clientSocket.getOutputStream());
        inputStream = new DataInputStream(clientSocket.getInputStream());
        isRunning = true;
        
        System.out.println("Connected to server: " + host + ":" + port);
        
        startReceiving();
    }
    
    /**
     * Starts a background thread to receive data.
     */
    private void startReceiving() {
        executorService.submit(() -> {
            try {
                while (isRunning) {
                    int length = inputStream.readInt();
                    if (length > 0) {
                        byte[] data = new byte[length];
                        inputStream.readFully(data);
                        
                        if (dataReceiver != null) {
                            dataReceiver.accept(data);
                        }
                    }
                }
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error receiving data: " + e.getMessage());
                    stop();
                }
            }
        });
    }
    
    /**
     * Sends data over the network.
     * @param data The data to send
     * @throws IOException If an I/O error occurs
     */
    public void transmit(byte[] data) throws IOException {
        if (!isRunning || outputStream == null) {
            throw new IOException("Not connected");
        }
        
        outputStream.writeInt(data.length);
        outputStream.write(data);
        outputStream.flush();
    }
    
    /**
     * Stops the network manager and closes all connections.
     */
    public void stop() {
        isRunning = false;
        
        try {
            if (outputStream != null) outputStream.close();
            if (inputStream != null) inputStream.close();
            if (clientSocket != null) clientSocket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing connections: " + e.getMessage());
        }
        
        outputStream = null;
        inputStream = null;
        clientSocket = null;
        serverSocket = null;
    }
}