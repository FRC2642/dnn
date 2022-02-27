package dnn;

/*
YOLONet Example Information
---------------------------

This is a basic implementation of a YOLO object detection network inference example.
It works with most variants of YOLO (YOLOv2, YOLOv2-tiny, YOLOv3, YOLOv3-tiny, YOLOv3-tiny-prn, YOLOv4, YOLOv4-tiny).
YOLO9000 is not support by OpenCV DNN.

To run download the following files and place them in the root folder of your project:

    YOLOv4 Configuration: https://raw.githubusercontent.com/AlexeyAB/darknet/master/cfg/yolov4.cfg
    YOLOv4 Weights: https://github.com/AlexeyAB/darknet/releases/download/darknet_yolo_v3_optimal/yolov4.weights
    COCO Names: https://raw.githubusercontent.com/AlexeyAB/darknet/master/data/coco.names
    Dog Demo Image: https://raw.githubusercontent.com/AlexeyAB/darknet/master/data/dog.jpg

For faster inferencing CUDA is highly recommended.
On CPU it is recommended to decrease the width & height of the network or use the tiny variants.
 */

import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.opencv.global.opencv_dnn;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_dnn.Net;
import org.bytedeco.opencv.opencv_text.FloatVector;
import org.bytedeco.opencv.opencv_text.IntVector;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import static org.bytedeco.opencv.global.opencv_core.CV_32F;
import static org.bytedeco.opencv.global.opencv_core.getCudaEnabledDeviceCount;
import static org.bytedeco.opencv.global.opencv_dnn.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgproc.LINE_8;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;


import static org.bytedeco.opencv.global.opencv_imgcodecs.*;



public class YOLONet {

    public static void main(String[] args) {
        // Mat image = imread("img2.jpg");

        YOLONet yolo = new YOLONet(
                "yolov4.cfg",
                "yolov4.weights",
                "coco.names",
                608, 608);
        yolo.setup();
        int i = 0;
        
        
        try (VideoCapture cam = new VideoCapture(0)) {
            Mat image = new Mat();
            while (cam.read(image)) {
                List<ObjectDetectionResult> results = yolo.predict(image);
                System.out.printf("Detected %d objects:\n", results.size());
                for(ObjectDetectionResult result : results) {
                    System.out.printf("\t%s - %.2f%%\n", result.className, result.confidence * 100f);
                    System.out.printf("X-Coordinate-%s\n", result.getMiddleX());
                    System.out.printf("Y-Coordinate-%s\n", result.getMiddleY());

                    // annotate on image
                    rectangle(image,
                            new Point(result.x, result.y),
                            new Point(result.x + result.width, result.y + result.height),
                            Scalar.MAGENTA, 2, LINE_8, 0);
                }

                String path = String.format("outs/out%s.jpg",i);
                imwrite(path, image);

                i = i + 1;
            }
        }
    }

    public static List<Double> predictOnCurrentFrame(int cam_num, String configPath, String weightsPath, String namesPath, int width, int height) {
        YOLONet yolo = new YOLONet(configPath, weightsPath, namesPath, width, height);
        yolo.setup();
        Mat image = new Mat();
        VideoCapture cam = new VideoCapture(cam_num);
        cam.read(image);
        List<ObjectDetectionResult> results = yolo.predict(image);
        List<Double> resultsFiltered = new ArrayList<Double>();
        int i = 0;
        for (ObjectDetectionResult result:results) {
            // check if result is ball
            // if (((result.className.equals("sports ball"))) || (result.className.equals("orange"))) {
                resultsFiltered.add(result.getMiddleX());
                System.out.print(result.className);
    
                rectangle(image,
                            new Point(result.x, result.y),
                            new Point(result.x + result.width, result.y + result.height),
                            Scalar.MAGENTA, 2, LINE_8, 0);
            // }
        }
        String path = String.format("outs/out%s.jpg",i);
        imwrite(path, image);
        i = i + 1;
        return resultsFiltered;
    }

    private Path configPath;
    private Path weightsPath;
    private Path namesPath;
    private int width;
    private int height;

    private float confidenceThreshold = 0.4f;
    private float nmsThreshold = 0.4f;

    private Net net;
    private StringVector outNames;

    private List<String> names;

    /**
     * Creates a new YOLO network.
     * @param configPath Path to the configuration file.
     * @param weightsPath Path to the weights file.
     * @param namesPath Path to the names file.
     * @param width Width of the network as defined in the configuration.
     * @param height Height of the network as defined in the configuration.
     */
    public YOLONet(String configPath, String weightsPath, String namesPath, int width, int height) {
        this.configPath = Paths.get(configPath);
        this.weightsPath = Paths.get(weightsPath);
        this.namesPath = Paths.get(namesPath);
        this.width = width;
        this.height = height;
    }

    /**
     * Initialises the network.
     *
     * @return True if the network initialisation was successful.
     */
    public boolean setup() {
        net = readNetFromDarknet(
                configPath.toAbsolutePath().toString(),
                weightsPath.toAbsolutePath().toString());

        // setup output layers
        outNames = net.getUnconnectedOutLayersNames();


        // enable cuda backend if available
        if (getCudaEnabledDeviceCount() > 0) {
            net.setPreferableBackend(opencv_dnn.DNN_BACKEND_CUDA);
            net.setPreferableTarget(opencv_dnn.DNN_TARGET_CUDA);
        }

        // read names file
        try {
            names = Files.readAllLines(namesPath);
        } catch (IOException e) {
            System.err.println("Could not read names file!");
            e.printStackTrace();
        }

        return !net.empty();
    }

    /**
     * Runs the object detection on the frame.
     *
     * @param frame Input frame.
     * @return List of objects that have been detected.
     */
    public List<ObjectDetectionResult> predict(Mat frame) {
        Mat inputBlob = blobFromImage(frame,
                1 / 255.0,
                new Size(width, height),
                new Scalar(0.0),
                true, false, CV_32F);

        net.setInput(inputBlob);
        Date exec_start = new Date();
        // run detection
        MatVector outs = new MatVector(outNames.size());
        net.forward(outs, outNames);
        Date exec_end = new Date();
        System.out.printf("Detected in %d milliseconds:\n", (exec_end.getTime() - exec_start.getTime()));


        // evaluate result
        List<ObjectDetectionResult> result = postprocess(frame, outs);

        // cleanup
        outs.releaseReference();
        inputBlob.release();

        return result;
    }

    public List<Double> predictQuick(Mat image){
        Mat inputBlob = blobFromImage(image,
                1 / 255.0,
                new Size(width, height),
                new Scalar(0.0),
                true, false, CV_32F);

        net.setInput(inputBlob);
        MatVector outs = new MatVector(outNames.size());
        net.forward(outs, outNames);
        List<ObjectDetectionResult> results = postprocess(image, outs);
        List<Double> resultsFiltered = new ArrayList<Double>();
        int i = 0;
        for (ObjectDetectionResult result:results) {
            // check if result is ball
//             if (((result.className.equals("sports ball"))) || (result.className.equals("orange"))) {
            resultsFiltered.add(result.getMiddleX());
//            rectangle(image,
//                    new Point(result.x, result.y),
//                    new Point(result.x + result.width, result.y + result.height),
//                    Scalar.MAGENTA, 2, LINE_8, 0);
//             }
        }
//        String path = String.format("outs/out%s.jpg",i);
//        imwrite(path, image);
        i = i + 1;
        return resultsFiltered;
    }

    /**
     * Remove the bounding boxes with low confidence using non-maxima suppression
     *
     * @param frame Input frame
     * @param outs  Network outputs
     * @return List of objects
     */
    private List<ObjectDetectionResult> postprocess(Mat frame, MatVector outs) {
        try (IntVector classIds = new IntVector()) {
            try (FloatVector confidences = new FloatVector()) {
                final RectVector boxes = new RectVector();

                for (int i = 0; i < outs.size(); ++i) {
                    // extract the bounding boxes that have a high enough score
                    // and assign their highest confidence class prediction.
                    Mat result = outs.get(i);
                    FloatIndexer data = result.createIndexer();

                    for (int j = 0; j < result.rows(); j++) {
                        // minMaxLoc implemented in java because it is 1D
                        int maxIndex = -1;
                        float maxScore = Float.MIN_VALUE;
                        for (int k = 5; k < result.cols(); k++) {
                            float score = data.get(j, k);
                            if (score > maxScore) {
                                maxScore = score;
                                maxIndex = k - 5;
                            }
                        }

                        if (maxScore > confidenceThreshold) {
                            int centerX = (int) (data.get(j, 0) * frame.cols());
                            int centerY = (int) (data.get(j, 1) * frame.rows());
                            int width = (int) (data.get(j, 2) * frame.cols());
                            int height = (int) (data.get(j, 3) * frame.rows());
                            int left = centerX - width / 2;
                            int top = centerY - height / 2;

                            classIds.push_back(maxIndex);
                            confidences.push_back(maxScore);

                            boxes.push_back(new Rect(left, top, width, height));
                        }
                    }

                    data.release();
                    result.release();
                }

                // remove overlapping bounding boxes with NMS
                IntPointer indices = new IntPointer(confidences.size());
                FloatPointer confidencesPointer = new FloatPointer(confidences.size());
                confidencesPointer.put(confidences.get());

                NMSBoxes(boxes, confidencesPointer, confidenceThreshold, nmsThreshold, indices, 1.f, 0);

                // create result list
                List<ObjectDetectionResult> detections = new ArrayList<>();
                for (int i = 0; i < indices.limit(); ++i) {
                    final int idx = indices.get(i);
                    final Rect box = boxes.get(idx);

                    final int clsId = classIds.get(idx);

                    detections.add(new ObjectDetectionResult() {{
                        classId = clsId;
                        className = names.get(clsId);
                        confidence = confidences.get(idx);
                        x = box.x();
                        y = box.y();
                        width = box.width();
                        height = box.height();
                    }});

                    box.releaseReference();
                }

                // cleanup
                indices.releaseReference();
                confidencesPointer.releaseReference();
                classIds.releaseReference();
                confidences.releaseReference();
                boxes.releaseReference();

                return detections;
            }
        }
    }

    /**
     * Dataclass for object detection result.
     */
    public static class ObjectDetectionResult {
        public int classId;
        public String className;

        public float confidence;

        public int x;
        public int y;
        public int width;
        public int height;

        public double getMiddleX() {
            double x = this.x + (this.width / 2);
            return x;
        }
        public double getMiddleY() {
            double y = this.y + (this.height / 2);
            return y;
        }
    }
}
