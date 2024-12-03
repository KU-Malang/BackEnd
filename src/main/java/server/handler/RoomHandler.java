package server.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import server.model.Room;
import server.util.ResponseBuilder;

public class RoomHandler {



    // 유저 요청 처리 메서드
    public void handleUserRequest(String requestJson, PrintWriter writer) {
        JsonObject request = JsonParser.parseString(requestJson).getAsJsonObject();
        int messageType = request.get("messageType").getAsInt();
        switch (messageType) {
            case 5: // 퀴즈 주제 선택
                //TODO- 퀴즈 주제 선택
                break;
            case 6: // 문제 출제
                //TODO- 문제 출제
                break;
            case 7: // 정답 제출
                //TODO- 정답 제출
                break;
            case 8: // 오답 제출
                //TODO- 오답 제출
                break;
            case 9: // 게임 결과 제공
                //TODO- 게임 결과 제공
                break;
            case 10: // 방 나가기
                //TODO- 방 나가기
                break;
            default:
                JsonObject errorResponse = new ResponseBuilder(messageType, "9999", "알 수 없는 요청입니다.")
                        .build();
                writer.println(errorResponse.toString());
        }
    }

}
