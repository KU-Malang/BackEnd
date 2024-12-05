package server.handler;

import com.google.gson.JsonObject;
import java.io.PrintWriter;
import server.manager.RoomManager;
import server.model.Room;
import server.util.ResponseBuilder;

public class RoomCreationHandler implements RequestHandler {

    private final RoomManager roomManager;

    public RoomCreationHandler(RoomManager roomManager) {
        this.roomManager = roomManager;
    }

    @Override
    public void handleRequest(JsonObject request, PrintWriter writer) {
        String roomName = request.get("roomName").getAsString();
        int maxPlayers = request.get("maxPlayers").getAsInt();
        int hostUserId = request.get("hostUserId").getAsInt();
        int quizCount = request.get("quizCount").getAsInt();

        // hostUserId가 유효한지 확인
        if (!roomManager.isValidHostUser(hostUserId)) {
            JsonObject errorResponse = new ResponseBuilder(2, "2002", "존재하지 않는 유저 ID입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 방 이름이 유효한지 확인
        if (!roomManager.isValidRoomName(roomName)) {
            JsonObject errorResponse = new ResponseBuilder(2, "2003", "방 이름은 1~7자 사이여야 합니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 퀴즈 인원 수가 유효한지 확인
        if (!roomManager.isValidMaxPlayers(maxPlayers)) {
            JsonObject errorResponse = new ResponseBuilder(2, "2004", "퀴즈 인원 수는 4~8인 사이여야 합니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 퀴즈 문제 수가 유효한지 확인
        if (!roomManager.isValidQuizCount(quizCount)) {
            JsonObject errorResponse = new ResponseBuilder(2, "2005", "퀴즈 문제 수는 10~50문제 사이여야 합니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 방 이름이 중복인지 확인
        if (roomManager.isDuplicateRoomName(roomName)) {
            JsonObject errorResponse = new ResponseBuilder(2, "2001", "이미 존재하는 방 이름입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 방 생성
        Room newRoom = roomManager.createRoom(roomName, maxPlayers, hostUserId, quizCount, writer);

        // 성공 응답
        JsonObject data = new JsonObject();
        data.addProperty("roomId", newRoom.getRoomId());

        JsonObject successResponse = new ResponseBuilder(2, "success", "방 생성 성공")
                .withData(data)
                .build();
        writer.println(successResponse.toString());
    }
}
