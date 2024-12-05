package server.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.PrintWriter;
import server.UserThread;
import server.manager.RoomManager;
import server.manager.UserManager;
import server.util.ResponseBuilder;

public class MainHandler {

    private final UserManager userManager;
    private final RoomManager roomManager;

    public MainHandler(UserManager userManager, RoomManager roomManager) {
        this.userManager = userManager;
        this.roomManager = roomManager;
    }

    public void mainHandler(String requestJson, PrintWriter writer, UserThread userThread) {
        try {
            JsonObject request = JsonParser.parseString(requestJson).getAsJsonObject();
            int messageType = request.get("messageType").getAsInt();

            switch (messageType) {
                case 1:
                    new LoginHandler(userManager).handleRequest(request, writer, userThread);
                    break;
                case 2:
                    new RoomCreationHandler(roomManager).handleRequest(request, writer);
                    break;
                case 3:
                    new RoomListHandler(roomManager, userManager).handleRequest(request, writer);
                    break;
                case 4:
                    new JoinRoomHandler(roomManager, userManager).handleRequest(request, writer);
                    break;
                case 5:
                    new TopicSelectionHandler(roomManager).handleRequest(request, writer);
                    break;
                case 6: // 문제 출제
                    new IssueQuestionHandler(roomManager).handleRequest(request, writer);
                    break;
                case 7: // 정답 제출
                    new CorrectAnswerHandler(roomManager).handleRequest(request, writer);
                    break;
                case 8: // 오답 제출
                    new WrongAnswerHandler(roomManager).handleRequest(request, writer);
                    break;
                case 9: // 게임 결과 제공
                    //TODO- 게임 결과 제공
                    break;
                case 10: // 방 나가기
                    //TODO- 방 나가기
                    break;
                case 11:
                    new GameStartHandler(roomManager).handleRequest(request, writer);
                    break;
                default:
                    JsonObject errorResponse = new ResponseBuilder(messageType, "9999", "알 수 없는 요청입니다.")
                            .build();
                    writer.println(errorResponse.toString());
            }
        } catch (Exception e) {
            System.out.println("요청 처리 중 오류: " + e.getMessage());
        }
    }
}
