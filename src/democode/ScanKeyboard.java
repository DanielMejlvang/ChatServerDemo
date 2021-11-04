package democode;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ScanKeyboard implements Runnable {
    Socket socket;

    public ScanKeyboard(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            Scanner scanner = new Scanner(socket.getInputStream());
            while (true) {
                System.out.println("From client: " + scanner.nextLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
