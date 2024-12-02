package server.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import server.handler.RoomHandler;
import server.model.Room;

public class RoomManager {

    private final Map<Integer, Room> rooms = new ConcurrentHashMap<>();
    private final UserManager userManager; // UserManager 주입

    private int nextRoomId = 1;

    public RoomManager(UserManager userManager) {
        this.userManager = userManager;
    }

    // 호스트 유저 확인
    public boolean isValidHostUser(int userid) {
        return userManager.isValidUser(userid);
    }

    // 방 이름 중복 확인
    public boolean isDuplicateRoomName(String roomName) {
        return rooms.values().stream()
                .anyMatch(room -> room.getRoomName().equalsIgnoreCase(roomName));
    }

    // 방 생성
    public synchronized Room createRoom(String roomName, int maxPlayers, int hostUserId, int quizCount) {
        RoomHandler roomHandler = new RoomHandler();
        int roomId = nextRoomId++;
        Room room = new Room(roomId, roomName, maxPlayers, hostUserId, quizCount, roomHandler);
        room.startThread(); // Room 내부 쓰레드 시작
        rooms.put(roomId, room);
        return room;
    }

    public synchronized void deleteRoom(int roomId) {
        Room room = rooms.remove(roomId);
        if (room != null) {
            room.stopThread(); // Room 내부 쓰레드 정지
        }
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
