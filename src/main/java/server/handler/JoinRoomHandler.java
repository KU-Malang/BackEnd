package server.handler;

import com.google.gson.JsonObject;
import java.io.PrintWriter;
import java.net.Socket;
import server.manager.RoomManager;
import server.manager.UserManager;
import server.model.Room;
import server.util.ResponseBuilder;

public class JoinRoomHandler {

    private final RoomManager roomManager;
    private final UserManager userManager;

    public JoinRoomHandler(RoomManager roomManager, UserManager userManager) {
        this.roomManager = roomManager;
        this.userManager = userManager;
    }

    public void handleJoinRoomRequest(JsonObject request, PrintWriter writer, Socket clientSocket) {
        int roomId = request.get("roomId").getAsInt();
        int userId = request.get("userId").getAsInt();

        // 방 확인
        Room room = roomManager.getRoom(roomId);

        // 방이 존재하지 않는 경우
        if (room == null) {
            JsonObject errorResponse = new ResponseBuilder(4, "error", "방이 존재하지 않습니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 메인 쓰레드와의 연결 종료
        userManager.disconnectFromMainThread(userId);

        // 방 입장 실패
        if (!room.addUser(userId, clientSocket)) {
            JsonObject errorResponse = new ResponseBuilder(4, "error", "방 입장 실패")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 방 입장 성공
        JsonObject data = new JsonObject();
        data.addProperty("roomId", room.getRoomId());
        data.addProperty("currentPlayers", room.getCurrentPlayers());
        data.addProperty("maxPlayers", room.getMaxPlayers());

        JsonObject successResponse = new ResponseBuilder(4, "success", "방 입장 성공")
                .withData(data)
                .build();
        writer.println(successResponse.toString());
    }
}
