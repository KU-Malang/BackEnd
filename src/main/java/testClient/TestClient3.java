package testClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TestClient3 {

    public static void main(String[] args) {
        Socket socket = null;
        PrintWriter writer = null;
        BufferedReader consoleReader = null;
        boolean endFlag = false;

        try {
            // 서버 연결
            socket = new Socket("127.0.0.1", 8080);

            // 입출력 스트림 초기화
            writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            consoleReader = new BufferedReader(new InputStreamReader(System.in));

            // 서버 메시지 수신용 InputThread 생성 및 시작
            InputThread inputThread = new InputThread(socket, serverReader);
            inputThread.start();

            System.out.println("서버와 연결되었습니다. 메시지를 입력하세요 ('quit' 입력 시 종료):");

            // 사용자 입력 처리
            String userInput;
            while ((userInput = consoleReader.readLine()) != null) {
                writer.println(userInput); // 서버로 메시지 전송
                writer.flush(); // 버퍼 비우기

                if ("quit".equalsIgnoreCase(userInput)) {
                    endFlag = true; // 종료 플래그 설정
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("클라이언트 오류 발생: " + e.getMessage());
        } finally {
            // 자원 정리
            try {
                if (consoleReader != null) {
                    consoleReader.close();
                }
                if (writer != null) {
                    writer.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println("자원 정리 중 오류 발생: " + e.getMessage());
            }
            System.out.println("클라이언트가 종료되었습니다.");
        }
    }
}
