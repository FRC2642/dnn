package dnn;

import java.lang.*;
import java.io.*;


public class ProcessOutStreamReader extends Thread {

	@Override
	public void run() {
		try {

                while (true) {
                    ByteArrayOutputStream buffer = Worker.getBuffer();
                    if (buffer != null) {
                    String data_string = new String(buffer.toByteArray());
                	Server.writeData(data_string);
                    }
       			} 
		}                
		catch (Exception ex) 
            {
                ex.printStackTrace();
            }
	}
}