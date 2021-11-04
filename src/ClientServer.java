import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class ClientServer {
    private Map<String, Socket> users = new TreeMap<>();

    public static void main(String[] args) {
        new ClientServer();
    }

    public ClientServer() {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            while (true) {
                Socket socket = serverSocket.accept(); //blocks
                users.put("unknownUser", socket);
                ClientHandler clientHandler = new ClientHandler(this, users);
                Thread handler = new Thread(clientHandler);
                handler.start();
            }
        } catch (IOException e) {
            System.out.println("Error in socket.");
            e.printStackTrace();
        }
    }

    public void sendToAll(String message) {
        for (Socket s: users.values()) {
            try {
                PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
                pw.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void whisper(String sender, Socket socket, String message) {
        try {
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
            pw.println("Whisper from " + sender + ":" + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String keyboardScan() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }
}
