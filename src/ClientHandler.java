import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private ClientServer server;
    private Map<String, Socket> users;

    public ClientHandler(ClientServer server, Map<String, Socket> users) {
        this.server = server;
        this.users = users;
    }

    @Override
    public void run() {
        boolean stop = false;
        String nickname;
        String message;
        System.out.println("New ClientHandler started.");
        try {
            //get nickname from new user
            PrintWriter pw = new PrintWriter(users.get("unknownUser").getOutputStream(), true);
            pw.println("Please input your nickname (no whitespaces allowed): ");
            Scanner scanner = new Scanner(users.get("unknownUser").getInputStream());
            Object object = new JSONParser().parse(scanner.nextLine());
            JSONObject receivedNickname = (JSONObject)object;
            nickname = receivedNickname.get("message").toString();
            users.put(nickname, users.get("unknownUser"));
            users.remove("unknownUser");
            //TODO: make sure nickname is unique
            pw.println("Nickname accepted. You can now begin chatting!");

            //new user in chat
            server.sendToAll(nickname + " has joined the chat.");
            server.sendToAll("Current online users: " + users.keySet().toString());
            pw.println("Type \"quit\" to stop connection.");
            pw.println("Use \"/w [nickname] [message]\" to send private message to specific user.");
            pw.println("Use \"/c [red, green, blue, yellow, black, purple, cyan, white]\" to color your message.");

            //listening to messages from client
            while (!stop) {
                Object temp = new JSONParser().parse(scanner.nextLine());
                JSONObject JSONmessage = (JSONObject)temp;
                message = JSONmessage.get("message").toString();
                if (message.equals("quit")) { //client wants to exit
                    server.sendToAll(nickname + " has left the chat.");
                    users.remove(nickname); //remove user and socket from map
                    server.sendToAll("Current online users: " + users.keySet().toString());
                    stop = true;
                } else if (message.startsWith("/w ")) { //client wants to send private message
                    Scanner whisperscan = new Scanner(message);
                    whisperscan.next();
                    String receiver = whisperscan.next();
                    String privateMessage = whisperscan.nextLine();
                    server.whisper(nickname, users.get(receiver), privateMessage);
                    pw.println("Whisper to " + receiver + ":" + privateMessage);
                } else if(!JSONmessage.get("color").toString().equals("default")) {
                    String color = JSONmessage.get("color").toString();
                    color = ansicolor(color);
                    server.sendToAll("From "+ nickname + ":" + color + message + "\u001B[0m");
                } else {
                    server.sendToAll("From "+ nickname + ": " + message);
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public String ansicolor(String color) {
        String ANSI_RESET = "\u001B[0m";
        String ANSI_BLACK = "\u001B[30m";
        String ANSI_RED = "\u001B[31m";
        String ANSI_GREEN = "\u001B[32m";
        String ANSI_YELLOW = "\u001B[33m";
        String ANSI_BLUE = "\u001B[34m";
        String ANSI_PURPLE = "\u001B[35m";
        String ANSI_CYAN = "\u001B[36m";
        String ANSI_WHITE = "\u001B[37m";
        switch (color) {
            case "blue":
                return ANSI_BLUE;
            case "red":
                return ANSI_RED;
            case "green":
                return ANSI_GREEN;
            case "yellow":
                return ANSI_YELLOW;
            case "black":
                return ANSI_BLACK;
            case "purple":
                return ANSI_PURPLE;
            case "cyan":
                return ANSI_CYAN;
            case "white":
                return ANSI_WHITE;
            default:
                return ANSI_RESET;
        }
    }
}