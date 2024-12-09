package server.handler;

import com.google.gson.JsonObject;
import java.io.PrintWriter;
import java.util.Map;
import server.enums.Topic;
import server.manager.RoomManager;
import server.model.Room;
import server.util.ResponseBuilder;

public class IssueQuestionHandler implements RequestHandler {

    private final RoomManager roomManager;

    public IssueQuestionHandler(RoomManager roomManager) {
        this.roomManager = roomManager;
    }

    @Override
    public void handleRequest(JsonObject request, PrintWriter writer) {
        int roomId = request.get("roomId").getAsInt();
        String quizType = request.get("quizType").getAsString();

        Room room = roomManager.getRoom(roomId);

        // 방이 존재하지 않는 경우
        if (room == null) {
            JsonObject errorResponse = new ResponseBuilder(6, "6001", "존재하지 않는 방 ID입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 문제 유형이 존재하지 않는 경우
        if (Topic.fromValue(quizType) == null) {
            JsonObject errorResponse = new ResponseBuilder(6, "6002", "존재하지 않는 문제 유형입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 게임이 진행 중인 방인지 확인
        if (!roomManager.isGameInProgress(roomId)) {
            JsonObject errorResponse = new ResponseBuilder(6, "6003", "게임이 진행 중인 방이 아닙니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 연습 문제가 출제되지 않았던 방인지 확인
        if (quizType.equals("PRACTICE")) {
            if (room.isPracticeIssued()) {
                JsonObject errorResponse = new ResponseBuilder(6, "6004", "이미 연습 문제가 출제되었던 방입니다.")
                        .build();
                writer.println(errorResponse.toString());
                return;
            } else {
                room.setPracticeIssued();
            }
        }

        // 패자부활전이 진행되지 않았던 방인지 확인
        if (quizType.equals("REDEMPTION")) {
            if (room.isRedemptionIssued()) {
                JsonObject errorResponse = new ResponseBuilder(6, "6005", "이미 패자부활전이 진행되었던 방입니다.")
                        .build();
                writer.println(errorResponse.toString());
                return;
            } else {
                // 패자부활전에 참여할 유저들을 설정
                roomManager.setRedemptionEligibleUsers(roomId);
                room.setRedemptionIssued();
            }
        }

        // 현재 문제 번호 계산
        int currentQuizIndex;
        switch (quizType) {
            case "PRACTICE":
                currentQuizIndex = 1;
                break;
            case "REDEMPTION":
                currentQuizIndex = room.getCurrentQuizCount() % 3 + 1;
                break;
            default:
                roomManager.incrementQuizCount(roomId);
                currentQuizIndex = room.getCurrentQuizCount();
                break;
        }

        // 유저들의 이번 문제 참여 여부를 가져옴
        Map<Integer, Boolean> userStatus = room.getUserStatus();

        userStatus.forEach((userId, isParticipating) -> {
            JsonObject data = new JsonObject();
            data.addProperty("topic", quizType);
            data.addProperty("quizNumber", currentQuizIndex);
            data.addProperty("userStatus", isParticipating);

            JsonObject successResponse = new ResponseBuilder(6, "success", "성공")
                    .withData(data)
                    .build();

            room.sendMessageToUser(userId, successResponse.toString());
        });
    }
}
