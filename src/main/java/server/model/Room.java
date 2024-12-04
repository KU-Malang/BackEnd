package server.model;

import java.io.PrintWriter;
import java.net.Socket;
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
    private final Map<Integer, Integer> userRating = new ConcurrentHashMap<>(); // 유저 점수 관리

    private final Map<Integer, PrintWriter> userWriter = new ConcurrentHashMap<>();

    private final RoomThread roomThread; // Room 자체 쓰레드 관리

    private final UserManager userManager;

    public Room(int roomId, String roomName, int maxPlayers, int hostUserId, int quizCount, UserManager userManager) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.maxPlayers = maxPlayers;
        this.hostUserId = hostUserId;
        this.quizCount = quizCount;
        this.roomThread= new RoomThread(this);
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
        int addUserRating=userManager.getUserById(userId).getRating();
        this.userRating.put(userId, addUserRating);
        return true;
    }



    // 특정 유저에게 메시지 전송
    public synchronized void sendMessageToUser(int userId, String message) {
        userWriter.get(userId).println(message);
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

}
