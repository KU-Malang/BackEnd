package server.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import server.model.User;

public class UserFileUtil {

    private static final String USER_FILE = "/home/ubuntu/BackEnd/user.txt"; // 절대 경로로 변경
    private int nextUserId = 1; // 새로운 유저 ID를 생성할 때 사용
    private final Map<Integer, User> users = new HashMap<>();

    public UserFileUtil() {
        load(); // 파일에서 유저 데이터를 로드
    }

    // 유저 데이터를 파일에서 로드
    public void load() {
        File file = new File(USER_FILE);

        if (!file.exists()) {
            System.out.println("유저 파일이 존재하지 않습니다: " + USER_FILE);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");

                if (parts.length == 4) { // userId 포함된 경우
                    int userId = Integer.parseInt(parts[0]);
                    String nickname = parts[1];
                    String password = parts[2];
                    int rating = Integer.parseInt(parts[3]);

                    users.put(userId, new User(userId, nickname, password, rating));
                    nextUserId = Math.max(nextUserId, userId + 1); // 가장 큰 userId 갱신
                }
            }
            System.out.println("유저 파일 로드 완료: " + users.size() + "명의 유저");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 새로운 유저 추가
    public synchronized void addUser(User user) {
        users.put(user.getUserId(), user);
        save(users);
    }

    // 유저 데이터를 파일에 저장
    public synchronized void save(Map<Integer, User> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (User user : users.values()) {
                writer.write(user.getUserId() + " " + user.getNickname() + " " + user.getPassword() + " "
                        + user.getRating());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 다음 유저 ID 반환
    public int getNextUserId() {
        return nextUserId++;
    }

    // 유저 데이터를 반환
    public Map<Integer, User> getUsers() {
        return users;
    }
}
