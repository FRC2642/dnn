#############################################
# Object detection - YOLO - OpenCV
# Author : Arun Ponnusamy   (July 16, 2018)
# Website : http://www.arunponnusamy.com
############################################


import cv2
import argparse
import numpy as np
import json

import datetime

class TinyArguments:
  classes='yolov3.txt'
  config='yolov3-tiny.cfg'
  image='w5Kffvn.jpg'
  weights='yolov3-tiny.weights'

class V4Arguments:
  classes='coco.names'
  config='yolov4.cfg'
  image='w5Kffvn.jpg'
  weights='yolov4.weights'

def simulate_network():
	print(json.dumps([1,2,3]))

def get_output_layers(net):
    
    layer_names = net.getLayerNames()
    
    output_layers = [layer_names[i - 1] for i in net.getUnconnectedOutLayers()]

    return output_layers

def get_output_layers(net):
    
    layer_names = net.getLayerNames()
    
    output_layers = [layer_names[i - 1] for i in net.getUnconnectedOutLayers()]

    return output_layers


def draw_prediction(img, class_id, confidence, x, y, x_plus_w, y_plus_h):

    label = str(classes[class_id])

    color = COLORS[class_id]

    cv2.rectangle(img, (x,y), (x_plus_w,y_plus_h), color, 2)

    cv2.putText(img, label, (x-10,y-10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)

def run_network():     

	args = V4Arguments()

	
	classes = None

	with open(args.classes, 'r') as f:
	    classes = [line.strip() for line in f.readlines()]

	COLORS = np.random.uniform(0, 255, size=(len(classes), 3))

	cap = cv2.VideoCapture(0)

	net = cv2.dnn.readNet(args.weights, args.config)

	print("network loaded")

	net.setPreferableBackend(cv2.dnn.DNN_BACKEND_CUDA)
	net.setPreferableTarget(cv2.dnn.DNN_TARGET_CUDA)

	while True:
		begin = datetime.datetime.now()

		ret, image = cap.read()

		Width = image.shape[1]
		Height = image.shape[0]
		scale = 0.00392

		blob = cv2.dnn.blobFromImage(image, scale, (416,416), (0,0,0), True, crop=False)

		net.setInput(blob)

		outs = net.forward(get_output_layers(net))

		end = datetime.datetime.now()
		print(end - begin)

		class_ids = []
		confidences = []
		boxes = []
		conf_threshold = 0.5
		nms_threshold = 0.4
		balls = []
		for out in outs:
			for detection in out:
				scores = detection[5:]
				class_id = np.argmax(scores)
				confidence = scores[class_id]
				if class_id == 32:
					if confidence > 0.5:
			            		center_x = int(detection[0] * Width)
			            		balls.append(center_x)
			            		# center_y = int(detection[1] * Height)
			            		# w = int(detection[2] * Width)
			            		# h = int(detection[3] * Height)
			            		# x = center_x - w / 2
			            		# y = center_y - h / 2
			            		# class_ids.append(class_id)
			            		# confidences.append(float(confidence))
			            		# boxes.append([x, y, w, h])


		# indices = cv2.dnn.NMSBoxes(boxes, confidences, conf_threshold, nms_threshold)

		# for i in indices:
		# 	box = boxes[i]
		# 	x = box[0]
		# 	y = box[1]
		# 	w = box[2]
		# 	h = box[3]
		# 	print(f"Center X: {(x + y)/2}")
		# del image
		# del indices
		print(json.dumps(balls))

run_network()