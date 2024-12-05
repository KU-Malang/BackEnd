package server.handler;

import com.google.gson.JsonObject;
import java.io.PrintWriter;
import server.manager.RoomManager;
import server.model.Room;
import server.model.User;
import server.util.ResponseBuilder;

public class ExitRoomHandler implements RequestHandler {

    private final RoomManager roomManager;

    public ExitRoomHandler(RoomManager roomManager) {
        this.roomManager = roomManager;
    }

    @Override
    public void handleRequest(JsonObject request, PrintWriter writer) {
        int roomId = request.get("roomId").getAsInt();
        int userId = request.get("userId").getAsInt();

        Room room = roomManager.getRoom(roomId);

        // 방이 존재하지 않는 경우
        if (room == null) {
            JsonObject errorResponse = new ResponseBuilder(10, "10001", "존재하지 않는 방 ID입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 유저가 존재하지 않는 경우
        if (!roomManager.isValidUser(userId)) {
            JsonObject errorResponse = new ResponseBuilder(10, "10002", "존재하지 않는 유저 ID입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 유저가 로그인되어 있지 않은 경우
        if (!roomManager.isLoginUser(userId)) {
            JsonObject errorResponse = new ResponseBuilder(10, "10003", "로그인되어 있지 않은 유저입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 방에 참여 중인 유저가 아닐 경우
        if (!roomManager.isUserInRoom(roomId, userId)) {
            JsonObject errorResponse = new ResponseBuilder(10, "10004", "방에 참여 중인 유저가 아닙니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 방 나가기
        roomManager.removeUserFromRoom(roomId, userId);

        if (roomManager.isGameInProgress(roomId)) {
            User user = roomManager.getUserById(userId);

            if (user.getRating() < 300) {
                user.setRating(0);
            } else {
                roomManager.decreaseRating(userId, 300);
            }
        }

        JsonObject successResponse = new ResponseBuilder(10, "success", "성공")
                .build();
        writer.println(successResponse.toString());

        // 본 유저가 호스트 유저였을 경우 다른 유저에게 넘김 (방에 남은 유저가 없는 경우 방 삭제)
        if (userId == room.getHostUserId()) {
            room.setHostUserId();
        }

        String roomName = room.getRoomName();
        String hostUser = roomManager.getUserNicknameById(room.getHostUserId());

        // 방 나가기 성공 응답
        JsonObject data = new JsonObject();
        data.addProperty("roomName", roomName);
        data.addProperty("hostUser", hostUser);
        data.add("userList", room.getUserNicknameList());

        JsonObject successResponseForRoom = new ResponseBuilder(10, "success", "성공")
                .withData(data)
                .build();
        room.broadcastMessage(successResponseForRoom.toString());
    }
}
