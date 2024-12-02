package server.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import server.manager.RoomManager;
import server.manager.UserManager;
import server.util.ResponseBuilder;

public class MainHandler implements Runnable {

    private final Socket clientSocket;
    private final RoomManager roomManager;
    private final UserManager userManager;

    public MainHandler(Socket clientSocket, RoomManager roomManager, UserManager userManager) {
        this.clientSocket = clientSocket;
        this.roomManager = roomManager;
        this.userManager = userManager;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            while (true) {
                String requestJson = reader.readLine();

                if (requestJson == null) {
                    System.out.println("클라이언트 연결이 종료되었습니다.");
                    break; // 루프 종료
                }

                try {
                    JsonObject request = JsonParser.parseString(requestJson).getAsJsonObject();
                    int messageType = request.get("messageType").getAsInt();

                    switch (messageType) {
                        case 1:
                            new LoginHandler(userManager).handleLogin(request, writer, clientSocket);
                            break;
                        case 2:
                            new RoomCreationHandler(roomManager).handleCreateRoom(request, writer, clientSocket);
                            break;
                        case 3:
                            new RoomListHandler(roomManager, userManager).handleRoomListRequest(request, writer);
                            break;
                        case 4:
                            new JoinRoomHandler(roomManager, userManager).handleJoinRoomRequest(request, writer,
                                    clientSocket);
                            break;
                        default:
                            JsonObject unknownRequestResponse = new ResponseBuilder(0, "9999", "알 수 없는 요청입니다.")
                                    .build();
                            writer.println(unknownRequestResponse.toString());
                            break;
                    }
                } catch (Exception e) {
                    System.out.println("예외 발생: " + e.getMessage());
                    JsonObject errorResponse = new ResponseBuilder(0, "9998", "잘못된 요청 형식입니다.")
                            .build();
                    writer.println(errorResponse.toString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
