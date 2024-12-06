package server;

import server.manager.RoomManager;
import server.model.Room;

public class RoomThread implements Runnable {

    private final Room room;
    private volatile boolean running = true;
    private final RoomManager roomManager;

    public RoomThread(Room room, RoomManager roomManager) {
        this.roomManager = roomManager;
        this.room = room;
    }

    @Override
    public void run() {
        while (running) {
            try {
                // 방에 유저가 있는지 확인
                checkRoomStatus();

                // 1분마다 확인 (60초 대기)
                Thread.sleep(60 * 1000); // 1분 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 방에 유저가 있는지 확인하고, 없으면 쓰레드를 종료.
     */
    private void checkRoomStatus() {
        if (room.getCurrentPlayers() == 0) {
            System.out.println("방 ID: " + room.getRoomId() + "에 사용자가 없습니다. 쓰레드를 종료합니다.");
            roomManager.deleteRoom(room.getRoomId());
        }
    }

    /**
     * 방 쓰레드 종료
     */
    public void stopThread() {
        running = false;
    }
}
