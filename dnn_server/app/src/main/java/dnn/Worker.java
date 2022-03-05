package dnn;


import org.opencv.opencv_videoio.VideoCapture;

import org.opencv.opencv_core.*;

import java.util.List;


public class Worker extends Thread {

    public Worker() {
    }

    @Override
    public void run() {
        VideoCapture cam = new VideoCapture(0);
        Mat image = new Mat();
        YOLONet net = new YOLONet( "yolov4.cfg", "yolov4.weights", "coco.names", 608, 608);
        net.setup();
        while (cam.read(image)) {
            Server.writeData(net.predictQuick(image));
        }
    }
}
