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
            JsonObject errorResponse = new ResponseBuilder(4, "4001", "존재하지 않는 방 ID입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 유저가 존재하지 않는 경우
        if (!userManager.isValidUser(userId)) {
            JsonObject errorResponse = new ResponseBuilder(4, "4002", "존재하지 않는 유저 ID입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 유저가 로그인되어 있지 않은 경우
        if (!userManager.isUserLoggedIn(userId)) {
            JsonObject errorResponse = new ResponseBuilder(4, "4005", "로그인되어 있지 않은 유저입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 메인 쓰레드와의 연결 종료
        userManager.disconnectFromMainThread(userId);

        // 방 입장 실패 - 인원이 다 찼거나 이미 참가중인 방인 경우
        if (!room.addUser(userId, clientSocket)) {
            JsonObject errorResponse = new ResponseBuilder(4, "4003", "인원이 다 찼거나 이미 참가중인 방입니다.")
                    .build();
            writer.println(errorResponse.toString());

            // 메인 쓰레드와의 연결 복구
            userManager.connectToMainThread(userId, clientSocket);

            return;
        }

        // 방 입장 실패 - 이미 게임이 진행 중인 방인 경우
        if (!room.isGameInProgress()) {
            JsonObject errorResponse = new ResponseBuilder(4, "4004", "이미 게임이 진행 중인 방입니다.")
                    .build();
            writer.println(errorResponse.toString());

            // 메인 쓰레드와의 연결 복구
            userManager.connectToMainThread(userId, clientSocket);

            return;
        }

        // 해당 방에 유저 추가
        room.addUser(userId, clientSocket);

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
