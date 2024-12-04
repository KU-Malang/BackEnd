package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import server.handler.MainHandler;
import server.manager.UserManager;

public class UserThread implements Runnable {

    private final Socket socket;
    private final MainHandler mainHandler; // 공유된 MainHandler 사용
    private final UserManager userManager; // UserManager 추가
    private volatile boolean running = true; // 쓰레드 상태 관리
    private int userId = 0; // 로그인되지 않은 상태의 기본값

    public UserThread(Socket socket, MainHandler mainHandler, UserManager userManager) {
        this.socket = socket;
        this.mainHandler = mainHandler;
        this.userManager = userManager;
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

                System.out.println("요청 처리 중: " + requestJson);
                mainHandler.mainHandler(requestJson, writer, this); // UserThread 인스턴스를 전달
            }
        } catch (IOException e) {
            System.out.println("클라이언트 연결 종료: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    public void stopThread() {
        running = false; // 루프 종료
        try {
            socket.close(); // 소켓 닫기
        } catch (IOException e) {
            System.out.println("소켓 종료 실패: " + e.getMessage());
        }
    }

    private void cleanup() {
        System.out.println("유저 쓰레드 종료");

        // userId가 설정된 경우 로그아웃 처리
        if (userId != 0) {
            userManager.logoutUser(userId);
            System.out.println("유저 ID: " + userId + "의 연결이 종료되었습니다. 로그아웃 처리 완료.");
        }

        stopThread();
    }

    // 로그인 성공 시 UserThread에 유저 ID를 설정하는 메서드 추가
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }
}
