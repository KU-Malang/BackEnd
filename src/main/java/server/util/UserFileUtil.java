package server.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import server.model.User;

public class UserFileUtil {

    private static final String USER_FILE = "/home/ubuntu/BackEnd/user.txt"; // 절대 경로로 변경

    private int nextUserId = 1; // 새로운 유저 ID를 생성할 때 사용
    private final Map<Integer, User> users = new ConcurrentHashMap<>();

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
        if (!users.containsKey(user.getUserId())) { // 중복 데이터 방지
            users.put(user.getUserId(), user);
            save(user); // 새로 추가된 데이터만 저장
        }
    }

    public synchronized void save(User user) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE, true))) { // append = true
            writer.write(
                    user.getUserId() + " " + user.getNickname() + " " + user.getPassword() + " " + user.getRating());
            writer.newLine();
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

    public synchronized void updateUserScore(int userId, int newRating) {
        // 해당 유저가 존재하는지 확인
        if (users.containsKey(userId)) {
            User user = users.get(userId);
            user.setRating(newRating); // 메모리 내의 데이터 업데이트

            // 파일 갱신
            updateFile();
        } else {
            System.out.println("User ID " + userId + " 에 해당하는 유저가 없습니다");
        }
    }

    // 파일 전체를 갱신하는 메서드
    private synchronized void updateFile() {
        File file = new File(USER_FILE);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) { // 덮어쓰기 모드
            for (User user : users.values()) {
                writer.write(user.getUserId() + " " + user.getNickname() + " " + user.getPassword() + " "
                        + user.getRating());
                writer.newLine();
            }
            System.out.println("유저 파일 갱신 완료.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
