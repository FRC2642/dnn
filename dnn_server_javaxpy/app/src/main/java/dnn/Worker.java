package dnn;


import java.util.List;

import java.lang.*;
import java.io.*;


public class Worker extends Thread {

    public Worker() {
    }

    @Override
    public void run() {
        while (true) {    
            try 
            {
                  
                // create a new process
                  
                ProcessBuilder builder = new ProcessBuilder("./run");
                Process pro = builder.start();


                InputStream in = pro.getInputStream();

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                int nRead;
                byte[] data = new byte[16384];

                while ((nRead = in.read(data, 0, data.length)) != -1) {
                  buffer.write(data, 0, nRead);
                }

                String data_string = new String(buffer.toByteArray());

                Server.writeData(data_string);          
                // kill the process
                pro.destroy();
          
            } 
                catch (Exception ex) 
            {
                ex.printStackTrace();
            }
        }
    }
}
