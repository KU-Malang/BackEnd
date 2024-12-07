package server.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import server.model.Room;
import server.model.User;

public class RoomManager {

    private final Map<Integer, Room> rooms = new ConcurrentHashMap<>();
    private final UserManager userManager; // UserManager 주입

    private int nextRoomId = 1;

    public RoomManager(UserManager userManager) {
        this.userManager = userManager;
    }

    // 유저 확인
    public boolean isValidUser(int userid) {
        return userManager.isValidUser(userid);
    }

    // 호스트 유저인지 확인
    public boolean isValidHostUser(int roomId, int userid) {
        return getRoom(roomId).getHostUserId() == userid;
    }

    // 로그인되어 있는 유저인지 확인
    public boolean isLoginUser(int userId) {
        return userManager.isUserLoggedIn(userId);
    }

    // 방에 참여 중인 유저인지 확인
    public boolean isUserInRoom(int roomId, int userId) {
        return getRoom(roomId).isUserInRoom(userId);
    }

    // 해당 방에서 유저 삭제
    public void removeUserFromRoom(int roomId, int userId) {
        getRoom(roomId).removeUser(userId);
    }

    // 방 이름 유효성 확인
    public boolean isValidRoomName(String roomName) {
        return !roomName.isEmpty() && roomName.length() <= 7;
    }

    // 퀴즈 인원 수 유효성 확인
    public boolean isValidMaxPlayers(int maxPlayers) {
        return maxPlayers >= 4 && maxPlayers <= 8;
    }

    // 퀴즈 문제 수 유효성 확인
    public boolean isValidQuizCount(int quizCount) {
        return quizCount >= 10 && quizCount <= 50;
    }

    // 방 이름 중복 확인
    public boolean isDuplicateRoomName(String roomName) {
        return rooms.values().stream()
                .anyMatch(room -> room.getRoomName().equalsIgnoreCase(roomName));
    }

    // 방 생성
    public synchronized Room createRoom(String roomName, int maxPlayers, int hostUserId, int quizCount) {
        int roomId = nextRoomId++;
        Room room = new Room(roomId, roomName, maxPlayers, hostUserId, quizCount, userManager,this);
        room.startThread(); // Room 내부 쓰레드 시작
        rooms.put(roomId, room);

        return room;
    }

    // 방 삭제
    public synchronized void deleteRoom(int roomId) {
        Room room = rooms.remove(roomId);
        if (room != null) {
            room.stopThread(); // 방 쓰레드 중지
            System.out.println("방 ID " + roomId + "가 삭제되었습니다.");
        } else {
            System.out.println("방 ID " + roomId + "를 찾을 수 없습니다.");
        }
    }

    // 게임 시작
    public void startGame(int roomId) {
        getRoom(roomId).startGame();
    }

    // 퀴즈 카운트 증가
    public void incrementQuizCount(int roomId) {
        getRoom(roomId).incrementQuizCount();
    }

    // 게임 진행 중인지 확인
    public boolean isGameInProgress(int roomId) {
        return getRoom(roomId).isGameInProgress();
    }

    // 유효한 문제 번호인지 확인
    public boolean isCorrectQuizIndex(int roomId, int quizIndex) {
        return getRoom(roomId).isCorrectQuizIndex(quizIndex);
    }

    // 패자부활전 대상 유저 설정
    public void setRedemptionEligibleUsers(int roomId) {
        getRoom(roomId).setRedemptionEligibleUsers();
    }

    // 정답 제출
    public void markAnswerSubmitted(int roomId, int quizIndex) {
        getRoom(roomId).markAnswerSubmitted(quizIndex);
    }

    // 정답 제출 여부 확인
    public boolean isAnswerSubmitted(int roomId, int quizIndex) {
        return getRoom(roomId).isAnswerSubmitted(quizIndex);
    }

    // 유저 정답 개수 증가
    public void incrementCorrectCount(int roomId, int userId) {
        getRoom(roomId).incrementCorrectCount(userId);
    }

    // 유저 ID로 유저 닉네임 조회
    public String getUserNickname(int userId) {
        return userManager.getUserNickname(userId);
    }

    // 유저 레이팅 증가
    public void increaseRating(int userId, int amount) {
        userManager.increaseRating(userId, amount);
    }

    // 유저 레이팅 감소
    public void decreaseRating(int userId, int amount) {
        userManager.decreaseRating(userId, amount);
    }

    // 유저 레이팅 갱신
    public void updateUserScore(int userId, int newRating) {
        userManager.updateUserScore(userId, newRating);
    }

    // 유저 ID로 유저 조회
    public User getUserById(int userId) {
        return userManager.getUserById(userId);
    }

    // 전체 방 조회
    public Map<Integer, Room> getAllRooms() {
        return rooms;
    }

    // 방 조회
    public Room getRoom(int roomId) {
        return rooms.get(roomId);
    }
}
