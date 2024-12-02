package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import server.handler.RoomHandler;
import server.model.Room;

public class UserThread implements Runnable {

    private final int userId;
    private final Socket socket;
    private final RoomHandler roomHandler;
    private final Room room;
    private volatile boolean running = true; // 쓰레드 상태 관리

    public UserThread(int userId, Socket socket, RoomHandler roomHandler, Room room) {
        this.userId = userId;
        this.socket = socket;
        this.roomHandler = roomHandler;
        this.room = room;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            while (running) {
                String requestJson = reader.readLine();
                if (requestJson == null) {
                    break; // 클라이언트 연결 종료
                }
                System.out.println("유저 요청 받음 - 유저 ID: " + userId + " -> " + requestJson);

                // 요청 처리
                roomHandler.handleUserRequest(requestJson, writer);
            }
        } catch (IOException e) {
            System.out.println("유저 ID: " + userId + " 연결 종료: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    public void sendMessage(String message) {
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(message);
            System.out.println("메시지 전송 성공 - 유저 ID: " + userId + " -> " + message);
        } catch (IOException e) {
            System.out.println("메시지 전송 실패 - 유저 ID: " + userId);
        }
    }

    public void stopThread() {
        running = false; // 루프 종료
        try {
            socket.close(); // 소켓 닫기
        } catch (IOException e) {
            System.out.println("소켓 종료 실패 - 유저 ID: " + userId);
        }
    }

    private void cleanup() {
        System.out.println("유저 쓰레드 종료 - 유저 ID: " + userId);
        room.removeUser(userId); // 방에서 유저 제거
    }
}
