package server.handler;

import com.google.gson.JsonObject;
import server.manager.RoomManager;
import server.model.Room;
import server.util.ResponseBuilder;

import java.io.PrintWriter;

public class RoomCreationHandler {
    private final RoomManager roomManager;

    public RoomCreationHandler(RoomManager roomManager) {
        this.roomManager = roomManager;
    }

    public void handleCreateRoom(JsonObject request, PrintWriter writer) {
        String roomName = request.get("roomName").getAsString();
        int maxPlayers = request.get("maxPlayers").getAsInt();
        int hostUserId = request.get("hostUserId").getAsInt();
        int quizCount = request.get("quizCount").getAsInt();

        //방 이름이 중복인지 확인
        if(roomManager.isDuplicateRoomName(roomName)){
            JsonObject errorResponse = new ResponseBuilder(2, "2001", "이미 존재하는 방 이름입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }
        //hostUserId가 유효한지 확인
        if(roomManager.isValidHostuser(hostUserId)) {
            JsonObject errorResponse = new ResponseBuilder(2, "2002", "존재하지 않는 유저 ID입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 방 생성
        Room newRoom = roomManager.createRoom(roomName, maxPlayers, hostUserId, quizCount);

        // 성공 응답
        JsonObject data = new JsonObject();
        data.addProperty("roomId", newRoom.getRoomId());

        JsonObject successResponse = new ResponseBuilder(2, "success", "방 생성 성공")
                .withData(data)
                .build();
        writer.println(successResponse.toString());


    }
}
