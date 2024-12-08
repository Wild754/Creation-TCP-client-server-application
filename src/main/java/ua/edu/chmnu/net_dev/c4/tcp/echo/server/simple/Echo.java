package ua.edu.chmnu.net_dev.c4.tcp.echo.server.simple;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Echo {
    private final static int DEFAULT_PORT = 6710;
    private static final List<PrintWriter> clientWriters = new ArrayList<>(); // Список підключених клієнтів
    private static boolean running = true; // Флаг для зупинки сервера

    // Метод для обробки кожного клієнта
    private static void processClient(Socket socket) {
        try (
                var ir = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                var writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            System.out.println("Connection established from: " + socket.getRemoteSocketAddress());

            // Додаємо клієнта до списку активних підключень
            synchronized (clientWriters) {
                clientWriters.add(writer);
            }

            String nick = ir.readLine();
            System.out.println("Client nick: " + nick);

            String message;
            while ((message = ir.readLine()) != null) {
                if (message.equalsIgnoreCase("Q")) {
                    break;
                }

                System.out.println("Received message from " + nick + ": " + message);

                // Пересилаємо повідомлення іншим клієнтам
                synchronized (clientWriters) {
                    for (PrintWriter clientWriter : clientWriters) {
                        if (clientWriter != writer) {
                            clientWriter.println("Message from " + nick + ": " + message);
                        }
                    }
                }
            }

            System.out.println("Client " + nick + " disconnected");
            synchronized (clientWriters) {
                clientWriters.remove(writer); // Видаляємо клієнта зі списку після відключення
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для зупинки сервера
    private static void stopServer(ServerSocket serverSocket) {
        running = false;
        try {
            serverSocket.close();
            System.out.println("Server is shutting down...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is running on port " + port);

            // Окремий потік для зупинки сервера за командою
            new Thread(() -> {
                try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
                    while (running) {
                        String command = consoleReader.readLine();
                        if ("exit".equalsIgnoreCase(command)) {
                            stopServer(serverSocket); // Викликаємо метод для зупинки сервера
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Основний цикл для обробки клієнтів
            while (running) {
                System.out.println("Waiting for a client...");
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("Client connected: " + socket.getRemoteSocketAddress());
                    new Thread(() -> processClient(socket)).start(); // Обробляємо кожного клієнта в окремому потоці
                } catch (IOException e) {
                    if (!running) {
                        System.out.println("Server stopped accepting new connections.");
                    } else {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server on port " + port + ": " + e.getMessage());
        }

        System.out.println("Server has been stopped.");
    }
}
