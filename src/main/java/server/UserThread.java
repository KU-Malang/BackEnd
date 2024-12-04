package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import server.handler.MainHandler;

public class UserThread implements Runnable {

    private final Socket socket;
    private final MainHandler mainHandler; // 공유된 MainHandler 사용
    private volatile boolean running = true; // 쓰레드 상태 관리

    public UserThread(Socket socket, MainHandler mainHandler) {
        this.socket = socket;
        this.mainHandler = mainHandler;
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
                mainHandler.mainHandler(requestJson, writer); // 요청 처리
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
        stopThread();
    }
}
