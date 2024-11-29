package server.manager;

import server.model.User;
import server.util.UserFileUtil;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    private final Map<Integer, User> users; // 유저 데이터 (userId -> User 객체)
    private final Map<Integer, Socket> loggedInUsers = new ConcurrentHashMap<>(); // 로그인된 유저 (userId -> Socket)
    private final UserFileUtil userFileUtil; // 유저 파일 유틸리티

    public UserManager() {
        this.userFileUtil = new UserFileUtil();
        this.users = new ConcurrentHashMap<>(userFileUtil.getUsers()); // 파일에서 유저 데이터 로드
    }

    // 유저 로그인
    public void connectToMainThread(int userId, Socket socket) {
        loggedInUsers.put(userId, socket); // 유저를 로그인 상태로 추가
    }

    // 유저 로그아웃
    public void disconnectFromMainThread(int userId) {
        loggedInUsers.remove(userId); // 로그인 상태에서 제거
    }

    // 유저 추가
    public synchronized void addUser(String nickname, String password) {
        int userId = userFileUtil.getNextUserId(); // 새로운 userId 할당
        User newUser = new User(userId, nickname, password);
        users.put(userId, newUser);
        userFileUtil.addUser(newUser); // 파일에 유저 데이터 추가
    }

    // 유저 데이터 저장
    public synchronized void saveUsers() {
        userFileUtil.save(users); // 파일에 저장
    }

    // 유저 조회
    public User getUser(int userId) {
        return users.get(userId);
    }

    // 로그인된 유저 확인
    public boolean isUserLoggedIn(int userId) {
        return loggedInUsers.containsKey(userId);
    }



    // 닉네임과 비밀번호로 유저 존재 여부 확인
    public boolean isValidNickname(String nickname) {
        for (User user : users.values()) {
            if (user.getNickname().equals(nickname)) {
                return true; // 유저닉네임이 존재함
            }
        }
        return false; // 유저 닉네임이 존재하지 않음
    }

    public boolean isValidUser(int userId) {
        for (User user : users.values()) {
            if (user.getUserId()==userId) {
                return true; // 유저 id가 존재함
            }
        }
        return false; // 유저 id가 존재하지 않음
    }

    public User getUserByCredentials(String nickname, String password) {
        for (User user : users.values()) {
            if (user.getNickname().equals(nickname) && user.getPassword().equals(password)) {
                return user; // 유저 객체 반환
            }
        }
        return null; // 유저를 찾지 못한 경우
    }
    public User getUserById(int userId) {
        return users.get(userId);
    }


}
