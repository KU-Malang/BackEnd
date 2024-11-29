package server.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import server.model.Room;
import server.util.ResponseBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class RoomHandler {

    // 방 쓰레드 실행
    public void runRoom(Room room) {
        System.out.println("방 시작: " + room.getRoomName());
        while (!Thread.currentThread().isInterrupted()) {
            try {
                processClientRequests(room); // 클라이언트 요청 처리
                Thread.sleep(1000); // 1초 대기
            } catch (InterruptedException e) {
                System.out.println("방 종료 요청: " + room.getRoomName());
                break;
            }
        }
    }

    // 클라이언트 요청 처리
    private void processClientRequests(Room room) {
        room.getUserSockets().forEach((userId, socket) -> {
            try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
                String requestJson = readFromSocket(socket);
                if (requestJson != null) {
                    handleUserRequest(requestJson, writer);
                }
            } catch (Exception e) {
                System.out.println("유저 [" + userId + "]의 요청 처리 중 오류 발생: " + e.getMessage());
                room.removeUser(userId);
            }
        });
    }

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
    private String readFromSocket(Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            return reader.readLine();
        } catch (IOException e) {
            System.out.println("소켓에서 데이터 읽기 실패: " + e.getMessage());
            return null;
        }
    }

}
