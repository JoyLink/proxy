import java.io.*;
import java.net.*;

public class ProxyServer {
    public static final boolean SingleThreaded = false;
    public static void main(String[] args) throws IOException {
        ServerSocket s = new ServerSocket(8080);
        while (true) {
            Socket channel = s.accept();
            Runnable  handler = new ClientHandler(channel);
            if (SingleThreaded) {
                handler.run();
            } else {
                Thread t = new Thread(handler);
                t.start();
            }
        }
    }
}

