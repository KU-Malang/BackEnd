package server.handler;

import com.google.gson.JsonObject;

import java.io.PrintWriter;
import java.net.Socket;

public interface RequestHandler {

    void handleRequest(JsonObject request, PrintWriter writer);
}
