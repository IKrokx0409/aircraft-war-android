package edu.hitsz.server;

import com.sun.net.httpserver.HttpServer;

import edu.hitsz.server.http.RecordHandler;
import edu.hitsz.server.socket.GameSocketHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

public class MainServer {

    public static void main(String[] args) throws IOException {
        // HTTP 排行榜服务 — 端口 8080
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        httpServer.createContext("/api/records", new RecordHandler());
        httpServer.setExecutor(Executors.newFixedThreadPool(4));
        httpServer.start();
        System.out.println("HTTP server started on http://localhost:8080");

        // Socket 实时对战服务 — 端口 9999
        ServerSocket serverSocket = new ServerSocket(9999);
        System.out.println("Socket server started on port 9999");

        new Thread(() -> {
            try {
                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    System.out.println("New connection: " + socket.getInetAddress());
                    new Thread(new GameSocketHandler(socket)).start();
                }
            } catch (IOException e) {
                System.err.println("Socket server stopped: " + e.getMessage());
            }
        }, "socket-accept").start();
    }
}
