package id.ac.stis.pbo.demo1.server;

import id.ac.stis.pbo.demo1.data.DataStore;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Multi-threaded server for GAWE application
 * Handles multiple client connections simultaneously
 */
public class GaweServer {
    private static final Logger logger = Logger.getLogger(GaweServer.class.getName());
    private static final int PORT = 8080;
    private static final int THREAD_POOL_SIZE = 10;
    
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private Gson gson;
    private boolean isRunning = false;

    public GaweServer() {
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.gson = new Gson();
    }

    public void start() {
        try {
            // Initialize data store
            DataStore.initialize();
            
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            
            logger.info("GAWE Server started on port " + PORT);
            logger.info("Waiting for client connections...");
            
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("New client connected: " + clientSocket.getInetAddress());
                    
                    // Handle client in thread pool
                    threadPool.submit(new ClientHandler(clientSocket, gson));
                    
                } catch (IOException e) {
                    if (isRunning) {
                        logger.warning("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            logger.severe("Failed to start server: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        isRunning = false;
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.warning("Error closing server socket: " + e.getMessage());
        }
        
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
        }
        
        logger.info("GAWE Server stopped");
    }

    public static void main(String[] args) {
        GaweServer server = new GaweServer();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        
        // Start server
        server.start();
    }
}
