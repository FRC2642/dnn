package dnn;

import java.net.*;

// THis doesn't make sense. I already have each client socket instance being handled in a seperate thread, it should be garbage collected at the death of that thread. This might be useful for something else.

public class SocketSingleton {

    private static SocketSingleton instance = null;

    private Socket client;

    private SocketSingleton() {} 

    public static SocketSingleton getInstance() {
        if (instance == null) {
            instance = new SocketSingleton();
        }
        return instance;
    }

    public void setAndHandleClient(Socket client) {
        this.client = client;
        ClientHandler handler = new ClientHandler(client);
        handler.start();
    }

    public Socket getClient() {
        return this.client;
    }

}
