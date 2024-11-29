package server.model;

import server.UserThread;
import server.handler.RoomHandler;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Room {
    private final int roomId;
    private final String roomName;
    private final int maxPlayers;
    private final int hostUserId;
    private final int quizCount;
    private final Map<Integer, Socket> userSockets = new ConcurrentHashMap<>();
    private final Map<Integer, UserThread> userThreadInstances = new ConcurrentHashMap<>(); // UserThread 인스턴스 관리
    private boolean gameInProgress = false;

    private Thread roomThread; // Room 자체 쓰레드 관리

    private final RoomHandler roomHandler; // 요청 처리 핸들러

    public Room(int roomId, String roomName, int maxPlayers, int hostUserId, int quizCount, RoomHandler roomHandler) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.maxPlayers = maxPlayers;
        this.hostUserId = hostUserId;
        this.quizCount = quizCount;
        this.roomHandler = roomHandler;
    }

    // 방 쓰레드 시작
    public void startThread() {
        if (roomThread == null || !roomThread.isAlive()) {
            roomThread = new Thread(() -> roomHandler.runRoom(this));
            roomThread.start();
        }
    }

    // 방 쓰레드 정지
    public void stopThread() {
        if (roomThread != null && roomThread.isAlive()) {
            roomThread.interrupt();
        }
    }

    // 유저 추가
    public synchronized boolean addUser(int userId, Socket socket) {
        if (userSockets.size() >= maxPlayers || userSockets.containsKey(userId)) {
            return false;
        }
        userSockets.put(userId, socket);
        startUserThread(userId, socket); // 유저 쓰레드 시작
        return true;
    }


    // 유저 쓰레드 시작
    private void startUserThread(int userId, Socket socket) {
        UserThread userThread = new UserThread(userId, socket, roomHandler, this);
        userThreadInstances.put(userId, userThread);
        new Thread(userThread).start();
    }

    // 유저 제거
    public synchronized void removeUser(int userId) {
        userSockets.remove(userId);
        UserThread userThread = userThreadInstances.remove(userId);
        if (userThread != null) {
            userThread.stopThread(); // UserThread 내부에서 안전하게 종료 처리
        }
    }

    // 특정 유저에게 메시지 전송
    public synchronized void sendMessageToUser(int userId, String message) {
        UserThread userThread = userThreadInstances.get(userId);
        if (userThread != null) {
            userThread.sendMessage(message);
        } else {
            System.out.println("유저 ID " + userId + "에 대한 UserThread를 찾을 수 없습니다.");
        }
    }

    // 모든 유저에게 메시지 브로드캐스트
    public synchronized void broadcastMessage(String message) {
        userThreadInstances.forEach((userId, userThread) -> {
            try {
                userThread.sendMessage(message);
            } catch (Exception e) {
                System.out.println("메시지 브로드캐스트 실패: 유저 ID " + userId);
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
        return userSockets.size();
    }
    public Map<Integer, Socket> getUserSockets() {
        return userSockets;
    }


    public Map<Integer, UserThread> getUserThreadInstances() {
        return userThreadInstances;
    }
}
