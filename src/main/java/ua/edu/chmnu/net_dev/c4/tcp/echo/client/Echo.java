package ua.edu.chmnu.net_dev.c4.tcp.echo.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Echo {
    private final static int DEFAULT_PORT = 6710;

    public static void main(String[] args) {
        String serverAddress = "localhost"; // Або IP адреса сервера
        int port = DEFAULT_PORT;

        try (Socket socket = new Socket(serverAddress, port);
             var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             var writer = new PrintWriter(socket.getOutputStream(), true);
             var scanner = new Scanner(System.in)) {

            System.out.println("Connected to server " + serverAddress + ":" + port);

            // Отримуємо нік від користувача
            System.out.print("Enter your nickname: ");
            String nick = scanner.nextLine();
            writer.println(nick); // Надсилаємо нік на сервер

            // Отримуємо повідомлення від сервера
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = reader.readLine()) != null) {
                        System.out.println(serverMessage); // Виводимо повідомлення від інших клієнтів
                    }
                } catch (IOException e) {
                    System.err.println("Error reading from server: " + e.getMessage());
                }
            }).start();

            // Надсилаємо повідомлення на сервер
            String message;
            while (true) {
                System.out.print("Enter message (Q to quit): ");
                message = scanner.nextLine();
                writer.println(message);

                if (message.equalsIgnoreCase("Q")) {
                    break; // Завершуємо роботу клієнта
                }
            }
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }
}
