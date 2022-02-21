// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc;


import java.net.http.*;
import java.net.*;

import java.util.List;

import com.google.gson.*;

/** Add your docs here. */
public class VisionClient {

    public static Integer[] getData() {
        try {
            URI uri = new URI("http://10.26.42.143:8080");
            HttpRequest request = HttpRequest.newBuilder()
              .uri(uri)
              // .timeout(Duration.of(10, SECONDS))
              .GET()
              .build();
            HttpClient.Builder builder = HttpClient.newBuilder();
            HttpClient client = builder.build();
            HttpResponse<String> response =client.send(request, HttpResponse.BodyHandlers.ofString());
            String response_string = response.body();
            Gson gson = new Gson();
            return gson.fromJson(response_string, Integer[].class);
          } catch (Exception e) {
            System.out.println(e.getStackTrace());
            return new Integer[] {-1};
        }
    }
}
