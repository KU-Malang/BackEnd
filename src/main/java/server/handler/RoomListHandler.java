package server.handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import server.manager.RoomManager;
import server.manager.UserManager;
import server.model.Room;
import server.model.User;
import server.util.ResponseBuilder;

import java.io.PrintWriter;

public class RoomListHandler {
    private final RoomManager roomManager;
    private final UserManager userManager;

    public RoomListHandler(RoomManager roomManager,UserManager userManager) {
        this.roomManager = roomManager;
        this.userManager = userManager;
    }

    public void handleRoomListRequest(JsonObject request,PrintWriter writer) {
        // 모든 방 목록 가져오기
        JsonArray roomsArray = new JsonArray();

        int userId = request.get("userId").getAsInt();
        // 유저 정보 가져오기
        User user = userManager.getUserById(userId);
        if (user == null) {
            // 유저가 존재하지 않을 경우 에러 응답
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
