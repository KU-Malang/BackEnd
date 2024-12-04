package server.handler;

import com.google.gson.JsonObject;
import java.io.PrintWriter;
import server.enums.Topic;
import server.manager.RoomManager;
import server.model.Room;
import server.util.ResponseBuilder;

public class TopicSelectionHandler implements RequestHandler {

    private final RoomManager roomManager;

    public TopicSelectionHandler(RoomManager roomManager) {
        this.roomManager = roomManager;
    }

    @Override
    public void handleRequest(JsonObject request, PrintWriter writer) {
        int roomId = request.get("roomId").getAsInt();
        String topic = request.get("topic").getAsString();

        Room room = roomManager.getRoom(roomId);

        // 방이 존재하지 않는 경우
        if (room == null) {
            JsonObject errorResponse = new ResponseBuilder(5, "5001", "존재하지 않는 방 ID입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 주제가 존재하지 않는 경우
        if (Topic.fromValue(topic) == null) {
            JsonObject errorResponse = new ResponseBuilder(5, "5002", "존재하지 않는 주제입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 주제 선택 성공
        JsonObject data = new JsonObject();
        data.addProperty("topic", topic);

        JsonObject successResponse = new ResponseBuilder(5, "success", "성공")
                .withData(data)
                .build();
        room.broadcastMessage(successResponse.toString());
    }
}
