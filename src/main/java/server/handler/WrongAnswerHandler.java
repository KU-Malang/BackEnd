package server.handler;

import com.google.gson.JsonObject;
import java.io.PrintWriter;
import server.manager.RoomManager;
import server.model.Room;
import server.util.ResponseBuilder;

public class WrongAnswerHandler implements RequestHandler {

    private final RoomManager roomManager;

    public WrongAnswerHandler(RoomManager roomManager) {
        this.roomManager = roomManager;
    }

    @Override
    public void handleRequest(JsonObject request, PrintWriter writer) {
        int roomId = request.get("roomId").getAsInt();
        int userId = request.get("userId").getAsInt();
        String wrongAnswer = request.get("wrongAnswer").getAsString();

        Room room = roomManager.getRoom(roomId);

        // 방이 존재하지 않는 경우
        if (room == null) {
            JsonObject errorResponse = new ResponseBuilder(8, "8001", "존재하지 않는 방 ID입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 유저가 존재하지 않는 경우
        if (!roomManager.isValidUser(userId)) {
            JsonObject errorResponse = new ResponseBuilder(8, "8002", "존재하지 않는 유저 ID입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 유저가 로그인되어 있지 않은 경우
        if (!roomManager.isLoginUser(userId)) {
            JsonObject errorResponse = new ResponseBuilder(8, "8003", "로그인되어 있지 않은 유저입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 게임이 진행 중인 방인지 확인
        if (!room.isGameInProgress()) {
            JsonObject errorResponse = new ResponseBuilder(8, "8004", "게임이 진행 중인 방이 아닙니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 방에 참여 중인 유저가 아닐 경우
        if (!roomManager.isUserInRoom(roomId, userId)) {
            JsonObject errorResponse = new ResponseBuilder(8, "8005", "방에 참여 중인 유저가 아닙니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 오답 제출 성공 응답
        JsonObject data = new JsonObject();
        data.addProperty("userId", userId);
        data.addProperty("wrongAnswer", wrongAnswer);

        JsonObject successResponse = new ResponseBuilder(8, "success", "성공")
                .withData(data)
                .build();
        room.broadcastMessage(successResponse.toString());
    }
}
