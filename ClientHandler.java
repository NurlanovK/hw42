

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            do {
                clientName = "User" + new Random().nextInt(1000);
            } while (ChatServer.clients.containsKey(clientName));

            ChatServer.clients.put(clientName, out);
            broadcast(clientName + " присоединился к чату!");

            String message;
            while ((message = in.readLine()) != null) {
                if (message.equalsIgnoreCase("/exit")) {
                    break;
                } else {
                    broadcast(clientName + ": " + message, clientName);
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка соединения с клиентом.");
        } finally {
            disconnect();
        }
    }

    private void broadcast(String message) {
        for (PrintWriter clientOut : ChatServer.clients.values()) {
            clientOut.println(message);
        }
    }

    private void broadcast(String message, String excludeUser) {
        for (Map.Entry<String, PrintWriter> entry : ChatServer.clients.entrySet()) {
            if (!entry.getKey().equals(excludeUser)) {
                entry.getValue().println(message);
            }
        }
    }

    private void disconnect() {
        if (clientName != null) {
            ChatServer.clients.remove(clientName);
            broadcast(clientName + " покинул чат.");
        }
        try {
            socket.close();
        } catch (IOException ignored) {}
    }
}
