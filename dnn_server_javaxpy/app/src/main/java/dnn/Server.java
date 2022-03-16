package dnn;

import java.util.List;

import java.net.*;

import java.io.*;

import java.util.ArrayList;

public class Server {

    private static volatile String data;

    public static String getData() {
        return Server.data;
    }

    public static void writeData(String dataWrite) {
        Server.data = dataWrite;
    }


    public static void main(String[] args) {
        Worker worker = new Worker();
        worker.start();
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                try (Socket client = serverSocket.accept()) {
                    ClientHandler clientHandler = new ClientHandler(client);
                    clientHandler.run();
                }
            }
        } catch (IOException e) {
            System.out.print(e.getStackTrace());
        }
    }
}