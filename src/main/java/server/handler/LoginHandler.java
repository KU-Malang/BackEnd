package server.handler;

import com.google.gson.JsonObject;
import java.io.PrintWriter;
import java.net.Socket;
import server.manager.UserManager;
import server.model.User;
import server.util.ResponseBuilder;

public class LoginHandler {

    private final UserManager userManager; // UserManager 주입

    public LoginHandler(UserManager userManager) {
        this.userManager = userManager;
    }

    public void handleLogin(JsonObject request, PrintWriter writer, Socket clientSocket) {
        String nickname = request.get("nickname").getAsString();
        String password = request.get("password").getAsString();

        if (nickname.length() >= 10) {
            JsonObject errorResponse = new ResponseBuilder(1, "1001", "유저 아이디가 10글자를 초과합니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 유저 아이디가 없는 경우 -> 회원가입 진행
        if (!userManager.isValidNickname(nickname)) {
            userManager.addUser(nickname, password);
        }

        // 비밀번호 일치 여부 확인
        User currentUser = userManager.getUserByCredentials(nickname, password);

        // 비밀번호가 일치하지 않는 경우
        if (currentUser == null) {
            JsonObject errorResponse = new ResponseBuilder(1, "1002", "비밀번호가 일치하지 않습니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 이미 로그인 중인 경우
        if (userManager.isUserLoggedIn(currentUser.getUserId())) {
            JsonObject errorResponse = new ResponseBuilder(1, "1003", "이미 로그인 중인 유저입니다.")
                    .build();
            writer.println(errorResponse.toString());
            return;
        }

        // 로그인 처리
        userManager.connectToMainThread(currentUser.getUserId(), clientSocket);

        // 성공 응답
        JsonObject data = new JsonObject();
        data.addProperty("userId", currentUser.getUserId());

        JsonObject successResponse = new ResponseBuilder(1, "success", "로그인 성공")
                .withData(data)
                .build();
        writer.println(successResponse.toString());
    }
}
