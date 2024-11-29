package server.util;

import com.google.gson.JsonObject;

public class ResponseBuilder {
    private final int messageType;
    private final String status;
    private final String message;
    private JsonObject data;

    public ResponseBuilder(int messageType, String status, String message) {
        this.messageType = messageType;
        this.status = status;
        this.message = message;
    }

    public ResponseBuilder withData(JsonObject data) {
        this.data = data;
        return this;
    }

    public JsonObject build() {
        JsonObject response = new JsonObject();
        response.addProperty("messageType", messageType);
        response.addProperty("status", status);
        response.addProperty("message", message);
        if (data != null) {
            response.add("data", data);
        }
        return response;
    }
}
