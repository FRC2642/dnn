package dnn;

import java.util.List;

import java.net.*;

import java.io.*;

import com.google.gson.*;

public class Server {
    public String getGreeting() {
        return "Hello World!";
    }

    private static void handleClient(Socket client) throws IOException {
        System.out.println("Debug: got new client " + client.toString());
        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
        StringBuilder requestBuilder = new StringBuilder();
        String line;
        while (!(line = br.readLine()).isBlank()) {
            requestBuilder.append(line + "\r\n");
        }
        String request = requestBuilder.toString();
        System.out.println(request);
        List<Double> results = YOLONet.predictOnCurrentFrame(0, "yolov4.cfg", "yolov4.weights", "coco.names", 608, 608);
        Gson gson = new Gson();
        String json_list = gson.toJson(results);
        System.out.print(json_list);
        OutputStream clientOutput = client.getOutputStream();
        clientOutput.write("HTTP/1.1 200 OK\r\n".getBytes());
        clientOutput.write(("ContentType: application/json\r\n").getBytes());
        clientOutput.write("\r\n".getBytes());
        clientOutput.write(json_list.getBytes());
        clientOutput.write("\r\n\r\n".getBytes());
        clientOutput.flush();
        client.close();
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            Runtime.getRuntime().addShutdownHook(new Thread(){public void run(){
                try {
                    serverSocket.close();
                    System.out.println("The server is shut down!");
                } catch (IOException e) { /* failed */ }
            }});
            while (true) {
                try (Socket client = serverSocket.accept()) {
                    handleClient(client);
                }
            }
        } catch (IOException e) {
            System.out.print(e.getStackTrace());
    }
    }
}