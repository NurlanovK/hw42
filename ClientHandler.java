

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
                } else if (message.startsWith("/name ")) {
                    changeName(message.substring(6));
                } else if (message.equals("/list")) {
                    listUsers();
                } else if (message.startsWith("/whisper ")) {
                    privateMessage(message);
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

    private void changeName(String newName) {
        if (newName.contains(" ") || ChatServer.clients.containsKey(newName)) {
            out.println("Ошибка: Некорректное или уже занятое имя.");
            return;
        }
        String oldName = clientName;
        ChatServer.clients.remove(oldName);
        clientName = newName;
        ChatServer.clients.put(clientName, out);
        out.println("Вы теперь известны как " + newName);
        broadcast("Пользователь " + oldName + " теперь известен как " + newName);
    }

    private void listUsers() {
        out.println("Подключенные пользователи: " + String.join(", ", ChatServer.clients.keySet()));
    }

    private void privateMessage(String message) {
        String[] parts = message.split(" ", 3);
        if (parts.length < 3) {
            out.println("Использование: /whisper имя_пользователя сообщение");
            return;
        }
        String targetUser = parts[1];
        String msg = parts[2];
        PrintWriter targetOut = ChatServer.clients.get(targetUser);
        if (targetOut != null) {
            targetOut.println("[Личное от " + clientName + "]: " + msg);
            out.println("[Личное для " + targetUser + "]: " + msg);
        } else {
            out.println("Пользователь " + targetUser + " не найден.");
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
