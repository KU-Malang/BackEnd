package testClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

// 서버로부터 오는 메시지를 처리하는 InputThread
class InputThread extends Thread {

    private Socket socket;
    private BufferedReader serverReader;

    public InputThread(Socket socket, BufferedReader serverReader) {
        this.socket = socket;
        this.serverReader = serverReader;
    }

    @Override
    public void run() {
        try {
            String messageFromServer;
            while ((messageFromServer = serverReader.readLine()) != null) {
                System.out.println("서버 응답: " + messageFromServer);
            }
        } catch (IOException e) {
            System.out.println("서버와의 연결이 종료되었습니다: " + e.getMessage());
        } finally {
            // 자원 정리
            try {
                if (serverReader != null) {
                    serverReader.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println("InputThread 자원 정리 중 오류 발생: " + e.getMessage());
            }
        }
    }
}
