package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import server.handler.MainHandler;
import server.manager.RoomManager;
import server.manager.UserManager;
import server.util.UserFileUtil;

public class GameServer {

    private static final int PORT = 8080;

    private static final UserFileUtil userFileUtil = new UserFileUtil();
    private static final UserManager userManager = new UserManager(userFileUtil);
    private static final RoomManager roomManager = new RoomManager(userManager);
    private static final MainHandler mainHandler = new MainHandler(userManager, roomManager); // 공유 가능한 MainHandler

    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("서버가 시작되었습니다. 포트: " + PORT);

            System.out.println("유저 파일을 읽어옵니다...");
            userFileUtil.load();

            // 안전한 종료를 위한 Shutdown Hook 추가
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("서버를 종료합니다. 모든 자원을 해제합니다...");
                threadPool.shutdown(); // 스레드 풀 종료
                try {
                    if (!serverSocket.isClosed()) {
                        serverSocket.close(); // 서버 소켓 닫기
                    }
                } catch (IOException e) {
                    System.out.println("서버 소켓 종료 중 예외 발생: " + e.getMessage());
                }
            }));

            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new UserThread(clientSocket, mainHandler, userManager)); // MainHandler 공유
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
