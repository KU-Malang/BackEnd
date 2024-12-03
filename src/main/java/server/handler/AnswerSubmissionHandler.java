package server.handler;


import com.google.gson.JsonObject;
import java.io.PrintWriter;
import server.model.Room;
import server.manager.RoomManager;
import server.util.ResponseBuilder;

public class AnswerSubmissionHandler   {

    private final RoomManager roomManager;

    public AnswerSubmissionHandler(RoomManager roomManager) {
        this.roomManager = roomManager;
    }

    public void handleRequest(JsonObject request, PrintWriter writer) {
        int userId = request.get("userId").getAsInt();
        int roomId = request.get("roomId").getAsInt();
        Room room = roomManager.getRoom(roomId);

        if (room != null) {
            room.addTask(() -> {
                // 점수 증가 로직 실행
                room.incrementScore(userId);

                // 응답 생성 및 전송

            });
        } else {
            JsonObject errorResponse = new ResponseBuilder(7, "fail", "방을 찾을 수 없습니다.")
                    .build();
            writer.println(errorResponse.toString());
        }
    }
}
