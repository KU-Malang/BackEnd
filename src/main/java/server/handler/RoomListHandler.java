package server.handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.PrintWriter;
import server.manager.RoomManager;
import server.manager.UserManager;
import server.model.Room;
import server.model.User;
import server.util.ResponseBuilder;

public class RoomListHandler implements RequestHandler {

    private final RoomManager roomManager;
    private final UserManager userManager;

    public RoomListHandler(RoomManager roomManager, UserManager userManager) {
        this.roomManager = roomManager;
        this.userManager = userManager;
    }

    @Override
    public void handleRequest(JsonObject request, PrintWriter writer) {
        // 모든 방 목록 가져오기
        JsonArray roomsArray = new JsonArray();

        int userId = request.get("userId").getAsInt();
        // 유저 정보 가져오기
        User user = userManager.getUserById(userId);

        // 유저가 존재하지 않는 경우
        if (user == null) {
            JsonObject errorResponse = new ResponseBuilder(3, "3001", "존재하지 않는 유저 ID입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        for (Room room : roomManager.getAllRooms().values()) {
            JsonObject roomJson = new JsonObject();
            roomJson.addProperty("roomId", room.getRoomId());
            roomJson.addProperty("roomName", room.getRoomName());
            roomJson.addProperty("quizCount", room.getQuizCount());
            roomJson.addProperty("currentPlayers", room.getCurrentPlayers());
            roomJson.addProperty("maxPlayers", room.getMaxPlayers());
            roomsArray.add(roomJson);
        }

        // 응답 생성
        JsonObject data = new JsonObject();
        data.add("rooms", roomsArray);

        JsonObject successResponse = new ResponseBuilder(3, "success", "방 목록 조회 성공")
                .withData(data)
                .build();
        writer.println(successResponse.toString());
    }
}
