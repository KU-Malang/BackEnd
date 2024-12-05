package server.handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.PrintWriter;
import java.util.Map;
import server.manager.RoomManager;
import server.manager.UserManager;
import server.model.Room;
import server.util.ResponseBuilder;

public class GameResultHandler implements RequestHandler {

    private UserManager userManager;
    private RoomManager roomManager;

    public GameResultHandler(UserManager userManager, RoomManager roomManager) {
        this.userManager = userManager;
        this.roomManager = roomManager;
    }

    @Override
    public void handleRequest(JsonObject request, PrintWriter writer) {
        int roomId = request.get("roomId").getAsInt();
        Room room = roomManager.getRoom(roomId);

        // 방이 존재하지 않는 경우
        if (room == null) {
            JsonObject errorResponse = new ResponseBuilder(9, "9001", "존재하지 않는 방 ID입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 게임이 진행 중인 방인지 확인
        if (!room.isGameInProgress()) {
            JsonObject errorResponse = new ResponseBuilder(9, "9002", "게임이 진행 중인 방이 아닙니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        Map<Integer, Integer> userCorrectCount = room.getUserCorrectCount();

        // 총 맞춘 정답 수와 평균 계산
        int totalCorrect = userCorrectCount.values().stream().mapToInt(Integer::intValue).sum();
        int userCount = userCorrectCount.size();
        double averageCorrect = (double) totalCorrect / userCount; // 평균 맞춘 개수

        final int K = 10; // 점수 변화 민감도

        // 결과 데이터를 담을 JSON 배열
        JsonObject response = new JsonObject();
        JsonObject data = new JsonObject();
        JsonArray usersArray = new JsonArray();

        userCorrectCount.forEach((userId, correctNum) -> {
            // 점수 변화 계산
            int scoreChange = (int) ((correctNum - averageCorrect) * K);

            // 현재 점수 가져오기
            int currentRating = userManager.getUserRating(userId);

            // 새로운 점수 계산
            int newRating = Math.max(0, currentRating + scoreChange);

            // 점수 업데이트
            userManager.updateUserScore(userId, newRating);

            // 유저 정보를 JSON으로 추가
            JsonObject userResult = new JsonObject();
            userResult.addProperty("userId", userId);
            userResult.addProperty("nickname", userManager.getUserNickname(userId)); // 닉네임 가져오기
            // 얻는 점수가 양수이면 +를 붙여서 반환, 음수면 스트링 그대로 반환
            userResult.addProperty("change", scoreChange >= 0 ? "+" + scoreChange : String.valueOf(scoreChange));
            userResult.addProperty("rating", newRating);
            usersArray.add(userResult);
        });

        // 최종 응답 데이터 생성
        data.add("users", usersArray);
        response.addProperty("messageType", 9);
        response.addProperty("status", "success");
        response.addProperty("message", "성공");
        response.add("data", data);

        // 클라이언트로 브로드캐스트
        writer.println(response);
    }
}
