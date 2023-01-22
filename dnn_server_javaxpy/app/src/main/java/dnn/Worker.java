package dnn;


import java.util.List;

import java.lang.*;
import java.io.*;


public class Worker extends Thread {

    public Worker() {
    }
    public static volatile InputStream input;

    public static void setInput(InputStream input) {
        Worker.input = input;
    }

    public static InputStream getInput() {
        return Worker.input;
    }

    public static volatile ByteArrayOutputStream buffer;

    public static void setBuffer(ByteArrayOutputStream buffer) {
        Worker.buffer = buffer;
    }

    public static ByteArrayOutputStream getBuffer() {
        return Worker.buffer;
    }

    @Override
    public void run() {
        try {
        ProcessBuilder builder = new ProcessBuilder("./run");
        Process pro = builder.start();


        InputStream in = pro.getInputStream();

        Worker.setInput(in);
        ProcessOutStreamReader reader = new ProcessOutStreamReader();
        reader.start();
        while (true) {                          
                    // create a new process

                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    Worker.setBuffer(buffer);
                    int nRead;
                    // byte[] data = new byte[16777216];
                    byte[] data = new byte[16384];
                    while ((nRead = in.read(data, 0, data.length)) != -1) {
                      Worker.buffer.write(data, 0, nRead);
                    }

            }
    }
    catch (Exception ex) 
                {
                    ex.printStackTrace();
                }
    }
}

