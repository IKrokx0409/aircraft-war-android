package edu.hitsz.network;

public class ServerConfig {
    public static final String SERVER_IP = "10.0.2.2";
    public static final int SOCKET_PORT = 9999;
    public static final int HTTP_PORT = 8080;
    public static final String BASE_URL = "http://" + SERVER_IP + ":" + HTTP_PORT;

    private ServerConfig() {}
}
