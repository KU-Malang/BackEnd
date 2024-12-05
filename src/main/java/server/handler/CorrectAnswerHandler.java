package server.handler;

import com.google.gson.JsonObject;
import java.io.PrintWriter;
import server.manager.RoomManager;
import server.model.Room;
import server.util.ResponseBuilder;

public class CorrectAnswerHandler implements RequestHandler {

    private final RoomManager roomManager;

    public CorrectAnswerHandler(RoomManager roomManager) {
        this.roomManager = roomManager;
    }

    @Override
    public void handleRequest(JsonObject request, PrintWriter writer) {
        int roomId = request.get("roomId").getAsInt();
        int userId = request.get("userId").getAsInt();
        int currentQuizIndex = request.get("currentQuizIndex").getAsInt();
        String correctAnswer = request.get("correctAnswer").getAsString();

        Room room = roomManager.getRoom(roomId);

        // 방이 존재하지 않는 경우
        if (room == null) {
            JsonObject errorResponse = new ResponseBuilder(7, "7001", "존재하지 않는 방 ID입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 유저가 존재하지 않는 경우
        if (!roomManager.isValidUser(userId)) {
            JsonObject errorResponse = new ResponseBuilder(7, "7002", "존재하지 않는 유저 ID입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 유저가 로그인되어 있지 않은 경우
        if (!roomManager.isLoginUser(userId)) {
            JsonObject errorResponse = new ResponseBuilder(7, "7003", "로그인되어 있지 않은 유저입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 게임이 진행 중인 방인지 확인
        if (!room.isGameInProgress()) {
            JsonObject errorResponse = new ResponseBuilder(7, "7004", "게임이 진행 중인 방이 아닙니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 현재 문제 번호가 유효한지 확인
        if (!roomManager.isCorrectQuizIndex(roomId, currentQuizIndex)) {
            JsonObject errorResponse = new ResponseBuilder(7, "7005", "잘못된 문제 번호입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 정답 제출 실패 - 먼저 정답을 제출한 유저가 있는 경우
        if (roomManager.isAnswerSubmitted(roomId, currentQuizIndex)) {
            JsonObject errorResponse = new ResponseBuilder(7, "7006", "먼저 정답을 제출한 유저가 있습니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 정답 제출
        roomManager.markAnswerSubmitted(roomId, userId);
        roomManager.incrementCorrectCount(roomId, userId);

        // 정답 제출 성공 응답
        JsonObject data = new JsonObject();
        data.addProperty("userId", userId);
        data.addProperty("correctAnswer", correctAnswer);

        JsonObject successResponse = new ResponseBuilder(7, "success", "성공")
                .withData(data)
                .build();
        room.broadcastMessage(successResponse.toString());
    }
}
