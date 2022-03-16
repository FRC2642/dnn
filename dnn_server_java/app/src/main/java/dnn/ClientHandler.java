package dnn;

import java.net.*;

import java.io.*;

import com.google.gson.*;

public class ClientHandler extends Thread {

    Socket client;

    public ClientHandler(Socket client) {
        this.client = client;
    }

    private void handleRequest() {
        try {
            System.out.println("Debug: got new client " + client.toString());
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            StringBuilder requestBuilder = new StringBuilder();
            String line;
            while (!(line = br.readLine()).isEmpty()) {
                requestBuilder.append(line + "\r\n");
            }
            String request = requestBuilder.toString();
            Gson gson = new Gson();
            String json_list = gson.toJson(Server.getData());
            OutputStream clientOutput = client.getOutputStream();
            clientOutput.write("HTTP/1.1 200 OK\r\n".getBytes());
            clientOutput.write(("ContentType: application/json\r\n").getBytes());
            clientOutput.write("\r\n".getBytes());
            clientOutput.write(json_list.getBytes());
            clientOutput.write("\r\n\r\n".getBytes());
            clientOutput.flush();
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        handleRequest();
    }
}
