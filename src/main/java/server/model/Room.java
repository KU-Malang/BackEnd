package server.model;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import server.RoomThread;
import server.manager.UserManager;

public class Room {

    private final int roomId;
    private final String roomName;
    private final int maxPlayers;
    private final int hostUserId;
    private final int quizCount;
    private boolean gameInProgress = false;
    private boolean isPracticeIssued = false; // 연습 문제 출제 여부
    private boolean isRedemptionIssued = false; // 패자부활전 문제 출제 여부
    private int currentQuizCount = 0;

    private final Map<Integer, Integer> userRating = new ConcurrentHashMap<>(); // 유저 점수 관리
    private final Map<Integer, PrintWriter> userWriter = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> userCorrectCount = new ConcurrentHashMap<>(); // 유저 정답 개수 관리
    private final Map<Integer, Boolean> userStatus = new ConcurrentHashMap<>(); // 유저 상태 관리
    private final Map<Integer, Boolean> answerSubmitted = new ConcurrentHashMap<>(); // 문제 정답 제출 여부 관리

    private final RoomThread roomThread; // Room 자체 쓰레드 관리

    private final UserManager userManager;

    public Room(int roomId, String roomName, int maxPlayers, int hostUserId, int quizCount, UserManager userManager) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.maxPlayers = maxPlayers;
        this.hostUserId = hostUserId;
        this.quizCount = quizCount;
        this.roomThread = new RoomThread(this);
        this.userManager = userManager;
    }

    // 방 쓰레드 시작
    public void startThread() {
        new Thread(roomThread).start();
    }

    // 방 쓰레드 정지
    public void stopThread() {
        roomThread.stopThread();
    }

    // 점수 증가 메서드
    public synchronized void incrementScore(int userId) {
        userRating.put(userId, userRating.getOrDefault(userId, 0) + 1);
        System.out.println("유저 " + userId + "의 점수가 증가했습니다: " + userRating.get(userId));
    }

    // 작업 추가
    public void addTask(Runnable task) {
        roomThread.addTask(task);
    }

    // 유저 추가
    public synchronized boolean addUser(int userId, PrintWriter writer) {
        if (userWriter.size() >= maxPlayers || userWriter.containsKey(userId)) {
            return false;
        }
        userWriter.put(userId, writer);
        int addUserRating = userManager.getUserById(userId).getRating();
        this.userRating.put(userId, addUserRating);
        return true;
    }

    // 특정 유저에게 메시지 전송
    public synchronized void sendMessageToUser(int userId, String message) {
        userWriter.get(userId).println(message);
        userWriter.get(userId).flush(); // 버퍼를 비워줌
    }

    // 모든 유저에게 메시지 브로드캐스트
    public synchronized void broadcastMessage(String message) {
        userWriter.forEach((userId, userWriter) -> {
            try {
                sendMessageToUser(userId, message);
            } catch (Exception e) {
                System.out.println("메시지 브로드캐스트 실패 - 유저 ID: " + userId);
            }
        });
    }

    // gameInProgress를 True로 바꾸어 게임 시작을 알리고,
    // 현재 방 안에 있는 유저들의 ID를 바탕으로 userCorrectCount, userStatus initialize 한다.
    public synchronized void startGame() {
        if (this.gameInProgress) {
            return;
        }

        this.gameInProgress = true;

        // userCorrectCount, userStatus 초기화
        userWriter.keySet().forEach(userId -> {
            userCorrectCount.put(userId, 0);
            userStatus.put(userId, true);
        });

        // answerSubmitted 초기화
        for (int i = 1; i <= quizCount; i++) {
            answerSubmitted.put(i, false);
        }
    }

    public synchronized void incrementQuizCount() {
        if (this.currentQuizCount < this.quizCount) {
            this.currentQuizCount += 1;
        }
    }

    // 패자부활전을 위한 메서드 - 상위 50%의 유저는 userStatus를 false로 설정
    public synchronized void setRedemptionEligibleUsers() {
        // userCorrectCount를 오름차순으로 정렬하고 유저 ID 리스트를 가져옴
        List<Map.Entry<Integer, Integer>> sortedUsers = userCorrectCount.entrySet().stream()
                .sorted(Map.Entry.comparingByValue()) // 점수를 기준으로 오름차순 정렬
                .toList();

        int totalUsers = sortedUsers.size();
        int halfUsers = totalUsers % 2 == 0 ? totalUsers / 2 : (totalUsers + 1) / 2;

        // 상위 50% 유저의 상태를 false로 설정 (하위 50%는 true로 유지)
        for (int i = halfUsers; i < totalUsers; i++) {
            int userId = sortedUsers.get(i).getKey();
            userStatus.put(userId, false);
            System.out.println("유저 " + userId + "의 상태가 false로 설정되었습니다 (패자부활전 제외 대상).");
        }

        // 나머지 유저(하위 50%)의 상태는 true로 유지
        for (int i = 0; i < halfUsers; i++) {
            int userId = sortedUsers.get(i).getKey();
            userStatus.put(userId, true);
            System.out.println("유저 " + userId + "의 상태가 true로 설정되었습니다 (패자부활전 대상).");
        }
    }

    // 현재 문제 번호에 대한 정답 제출 여부를 true로 설정
    public synchronized void markAnswerSubmitted(int quizIndex) {
        answerSubmitted.put(quizIndex, true);
    }

    // 정답 제출 여부 확인
    public boolean isAnswerSubmitted(int quizIndex) {
        return answerSubmitted.get(quizIndex);
    }

    // 유효한 문제 번호인지 확인
    public boolean isCorrectQuizIndex(int quizIndex) {
        return answerSubmitted.containsKey(quizIndex);
    }

    // 유저 정답 개수 증가
    public synchronized void incrementCorrectCount(int userId) {
        userCorrectCount.put(userId, userCorrectCount.getOrDefault(userId, 0) + 1);
        System.out.println("유저 " + userId + "의 정답 개수가 증가했습니다: " + userCorrectCount.get(userId));
    }

    public void setPracticeIssued() {
        isPracticeIssued = true;
    }

    public void setRedemptionIssued() {
        isRedemptionIssued = true;
    }

    // Getters
    public int getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getHostUserId() {
        return hostUserId;
    }

    public int getQuizCount() {
        return quizCount;
    }

    public boolean isGameInProgress() {
        return gameInProgress;
    }

    public void setGameInProgress(boolean gameInProgress) {
        this.gameInProgress = gameInProgress;
    }

    public int getCurrentPlayers() {
        return userWriter.size();
    }

    public Map<Integer, PrintWriter> getUserWriter() {
        return userWriter;
    }

    public Map<Integer, Integer> getUserCorrectCount() {
        return userCorrectCount;
    }

    public Map<Integer, Boolean> getUserStatus() {
        return userStatus;
    }

    public boolean isPracticeIssued() {
        return isPracticeIssued;
    }

    public boolean isRedemptionIssued() {
        return isRedemptionIssued;
    }
}
