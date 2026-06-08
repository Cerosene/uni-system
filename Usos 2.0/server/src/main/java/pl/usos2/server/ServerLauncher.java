package pl.usos2.server;

import pl.usos2.server.config.ApplicationContext;
import pl.usos2.server.config.DemoDataInitializer;
import pl.usos2.server.network.protocol.RequestDispatcher;
import pl.usos2.server.network.session.SessionManager;
import pl.usos2.server.network.socket.UsosSocketServer;

import java.util.logging.Logger;

public class ServerLauncher {
    private static final Logger logger = Logger.getLogger(ServerLauncher.class.getName());

    private static final int DEFAULT_PORT = 5555;
    private static final int DEFAULT_THREADS = 10;

    public static void main(String[] args) {
        int port = readIntProperty("usos.server.port", DEFAULT_PORT);
        int threads = readIntProperty("usos.server.threads", DEFAULT_THREADS);
        boolean seedDemoData = Boolean.parseBoolean(System.getProperty("usos.demo.seed", "true"));

        ApplicationContext context = new ApplicationContext();
        if (seedDemoData) {
            logger.info("Initializing demo data. Disable with -Dusos.demo.seed=false if database is already seeded.");
            DemoDataInitializer.initialize(context);
        }

        SessionManager sessionManager = new SessionManager();
        RequestDispatcher dispatcher = new RequestDispatcher(context, sessionManager);
        new UsosSocketServer(port, threads, dispatcher).start();
    }

    private static int readIntProperty(String name, int defaultValue) {
        String value = System.getProperty(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(value.trim());
    }
}
