package pl.usos2.server.network.socket;

import pl.usos2.server.network.protocol.RequestDispatcher;
import pl.usos2.server.network.request.ClientRequest;
import pl.usos2.server.network.response.ServerResponse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    private final Socket socket;
    private final RequestDispatcher dispatcher;

    public ClientHandler(Socket socket, RequestDispatcher dispatcher) {
        this.socket = socket;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        try (Socket clientSocket = socket;
             ObjectOutputStream output = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(clientSocket.getInputStream())) {

            output.flush();
            Object rawRequest = input.readObject();
            if (!(rawRequest instanceof ClientRequest request)) {
                output.writeObject(ServerResponse.error("Invalid request payload."));
                output.flush();
                return;
            }

            ServerResponse response = dispatcher.dispatch(request);
            output.writeObject(response);
            output.flush();
        } catch (Exception exception) {
            logger.warning("Client handler error: " + exception.getMessage());
        }
    }
}
