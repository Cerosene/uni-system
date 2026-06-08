package pl.usos2.server.network.response;

import java.io.Serializable;

public class ServerResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final Object data;
    private final String errorMessage;
    private final String errorType;

    private ServerResponse(boolean success, Object data, String errorMessage, String errorType) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
        this.errorType = errorType;
    }

    public static ServerResponse ok(Object data) {
        return new ServerResponse(true, data, null, null);
    }

    public static ServerResponse ok() {
        return ok(null);
    }

    public static ServerResponse error(Throwable throwable) {
        String message = throwable.getMessage() == null ? throwable.getClass().getSimpleName() : throwable.getMessage();
        return new ServerResponse(false, null, message, throwable.getClass().getSimpleName());
    }

    public static ServerResponse error(String message) {
        return new ServerResponse(false, null, message, "IllegalStateException");
    }

    public boolean isSuccess() {
        return success;
    }

    public Object getData() {
        return data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorType() {
        return errorType;
    }
}
