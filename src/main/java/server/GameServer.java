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
    static UserManager userManager = new UserManager();
    static RoomManager roomManager = new RoomManager(userManager);

    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("서버가 시작되었습니다. 포트: " + PORT);

            System.out.println("유저 파일을 읽어옵니다...");
            userFileUtil.load();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new MainHandler(clientSocket, roomManager, userManager));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
