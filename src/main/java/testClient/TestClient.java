package testClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TestClient {

    public static void main(String[] args) {
        try (Socket socket = new Socket("127.0.0.1", 8080);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {

            String userInput;
            while ((userInput = consoleReader.readLine()) != null) {
                writer.println(userInput);  // 서버로 메시지 전송
                String response = reader.readLine();  // 서버의 응답 읽기
                System.out.println("서버 응답: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
