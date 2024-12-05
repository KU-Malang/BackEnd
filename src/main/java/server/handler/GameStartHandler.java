package server.handler;

import com.google.gson.JsonObject;
import java.io.PrintWriter;
import server.manager.RoomManager;
import server.model.Room;
import server.util.ResponseBuilder;

public class GameStartHandler implements RequestHandler {

    private final RoomManager roomManager;

    public GameStartHandler(RoomManager roomManager) {
        this.roomManager = roomManager;
    }

    @Override
    public void handleRequest(JsonObject request, PrintWriter writer) {
        int roomId = request.get("roomId").getAsInt();
        int userId = request.get("userId").getAsInt();

        Room room = roomManager.getRoom(roomId);

        // 방이 존재하지 않는 경우
        if (room == null) {
            JsonObject errorResponse = new ResponseBuilder(11, "11001", "존재하지 않는 방 ID입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // hostUserId가 유효한지 확인
        if (!roomManager.isValidUser(userId)) {
            JsonObject errorResponse = new ResponseBuilder(11, "11002", "존재하지 않는 유저 ID입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // hostUserId가 로그인되어 있는지 확인
        if (!roomManager.isLoginUser(userId)) {
            JsonObject errorResponse = new ResponseBuilder(11, "11003", "로그인되어 있지 않은 유저입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // user가 방의 host인지 확인
        if (!roomManager.isValidHostUser(roomId, userId)) {
            JsonObject errorResponse = new ResponseBuilder(11, "11004", "해당 유저는 방장이 아닙니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 이미 게임이 진행 중인 방인지 확인
        if (room.isGameInProgress()) {
            JsonObject errorResponse = new ResponseBuilder(11, "11005", "이미 게임이 진행 중인 방입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 방에 참여 중인 유저가 아닐 경우
        if (!roomManager.isUserInRoom(roomId, userId)) {
            JsonObject errorResponse = new ResponseBuilder(11, "11006", "방에 참여 중인 유저가 아닙니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 게임 시작
        roomManager.startGame(roomId);

        // 게임 시작 성공
        JsonObject successResponse = new ResponseBuilder(11, "success", "게임 시작")
                .build();
        room.broadcastMessage(successResponse.toString());
    }
}
