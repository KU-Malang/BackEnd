package server;

import server.model.Room;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RoomThread implements Runnable {

    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private final Room room;
    private volatile boolean running = true;

    public RoomThread(Room room) {
        this.room = room;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Runnable task = taskQueue.take();
                task.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void addTask(Runnable task) {
        taskQueue.offer(task);
    }

    public void stopThread() {
        running = false;
        taskQueue.offer(() -> {}); // 큐를 깨워서 쓰레드 종료
    }
}
