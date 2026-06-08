package pl.usos2.client.network;

import pl.usos2.server.network.request.ClientRequest;
import pl.usos2.server.network.response.ServerResponse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ApiClient {
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5555;

    private final String host;
    private final int port;

    public ApiClient() {
        this(System.getProperty("usos.server.host", DEFAULT_HOST), readPort());
    }

    public ApiClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Object send(String action, String token, Map<String, Object> payload) {
        ClientRequest request = new ClientRequest(action, token, payload);
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {

            output.flush();
            output.writeObject(request);
            output.flush();

            Object rawResponse = input.readObject();
            if (!(rawResponse instanceof ServerResponse response)) {
                throw new IllegalStateException("Server returned invalid response payload.");
            }
            if (!response.isSuccess()) {
                throw new IllegalStateException(response.getErrorMessage());
            }
            return response.getData();
        } catch (ConnectException exception) {
            throw new IllegalStateException("Nie można połączyć się z serwerem USOS 2.0 na " + host + ":" + port
                    + ". Najpierw uruchom pl.usos2.server.ServerLauncher.", exception);
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Błąd komunikacji klient-serwer: " + exception.getMessage(), exception);
        }
    }

    public Object send(String action, String token) {
        return send(action, token, Map.of());
    }

    public Map<String, Object> payload(Object... values) {
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("Payload requires key/value pairs.");
        }
        Map<String, Object> payload = new HashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            payload.put(String.valueOf(values[i]), values[i + 1]);
        }
        return payload;
    }

    private static int readPort() {
        String value = System.getProperty("usos.server.port");
        if (value == null || value.isBlank()) {
            return DEFAULT_PORT;
        }
        return Integer.parseInt(value.trim());
    }
}
