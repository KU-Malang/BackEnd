package server.handler;

import com.google.gson.JsonObject;
import java.io.PrintWriter;

public interface RequestHandler {

    void handleRequest(JsonObject request, PrintWriter writer);
}
