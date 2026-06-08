package pl.usos2.server.network.socket;

import pl.usos2.server.network.protocol.RequestDispatcher;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class UsosSocketServer {
    private static final Logger logger = Logger.getLogger(UsosSocketServer.class.getName());

    private final int port;
    private final int maxThreads;
    private final RequestDispatcher dispatcher;
    private volatile boolean running;

    public UsosSocketServer(int port, int maxThreads, RequestDispatcher dispatcher) {
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Invalid server port: " + port);
        }
        if (maxThreads <= 0) {
            throw new IllegalArgumentException("Thread pool size must be positive.");
        }
        this.port = port;
        this.maxThreads = maxThreads;
        this.dispatcher = Objects.requireNonNull(dispatcher, "RequestDispatcher cannot be null.");
    }

    public void start() {
        running = true;
        ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("USOS 2.0 socket server started on port " + port + ", workerThreads=" + maxThreads);
            while (running) {
                Socket socket = serverSocket.accept();
                logger.info("Accepted client: " + socket.getRemoteSocketAddress());
                executorService.submit(new ClientHandler(socket, dispatcher));
            }
        } catch (IOException exception) {
            if (running) {
                throw new IllegalStateException("USOS socket server failed.", exception);
            }
        } finally {
            executorService.shutdownNow();
        }
    }

    public void stop() {
        running = false;
    }
}
