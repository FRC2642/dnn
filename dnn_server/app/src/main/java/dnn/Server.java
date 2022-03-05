package dnn;

import java.util.List;

import java.net.*;

import java.io.*;

import com.google.gson.*;

import java.util.ArrayList;

public class Server {

    public static volatile List<Double> data = new ArrayList<>();

    public static List<Double> getData() {
        return Server.data;
    }

    public static void writeData(List<Double> dataWrite) {
        Server.data.clear();
        Server.data.addAll(dataWrite);
    }

//    Because I don't know how tests work and I don't want to mess with them.
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        System.load("opencv")
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