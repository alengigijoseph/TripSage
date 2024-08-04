import cv2
import pandas as pd
import numpy as np
from ultralytics import YOLO
from tracker import*
import time
from math import dist
import requests

model=YOLO('yolov8s.pt')
my_file = open("coco.txt", "r")
data = my_file.read()
class_list = data.split("\n")

MAX_CARS = 10 
PUBLISH_INTERVAL = 600 
speed_limit = 80 
road_length = 1000
camera_id = 'CAM002'
road_name = 'NH44'
url = 'https://4d4f-2409-40f3-101d-3564-3db7-c774-e7f-9836.ngrok-free.app/route/savedata'

def send_data_to_server(road_name, camera_id, time_est):
    try:
        data = {
            'roadName': road_name,
            'cameraId': camera_id,
            'timeEst': time_est
        }

        response = requests.post(url, json=data)

        if response.status_code == 201:
            print('Route data saved successfully')
        else:
            print('Failed to save route data:', response.text)
    except Exception as e:
        print('Error sending data to server:', e)


speeds=[]

def detect_speed():

    cy1=322
    cy2=368
    offset=6
    tracker=Tracker()
    vh_down={}
    counter=[]
    total_cars_detected = 0

    cap=cv2.VideoCapture('veh2.mp4')

    while True:    
        
        ret,frame = cap.read()
        if not ret:
            break
        
        frame_number = int(cap.get(cv2.CAP_PROP_POS_FRAMES))

        if total_cars_detected >= MAX_CARS:
            cap.release()
            cv2.destroyAllWindows()
            total_cars_detected = 0
            break

        if frame_number % 3 != 0:
            continue

        frame=cv2.resize(frame,(1020,500))
    

        results=model.predict(frame)
        a=results[0].boxes.data
        px=pd.DataFrame(a).astype("float")
        list=[]
                
        for index,row in px.iterrows():
    #        print(row)
    
            x1=int(row[0])
            y1=int(row[1])
            x2=int(row[2])
            y2=int(row[3])
            d=int(row[5])
            c=class_list[d]
            if 'car' in c:
                list.append([x1,y1,x2,y2])
        bbox_id=tracker.update(list)
        for bbox in bbox_id:
            x3,y3,x4,y4,id=bbox
            cx=int(x3+x4)//2
            cy=int(y3+y4)//2
            
            cv2.rectangle(frame,(x3,y3),(x4,y4),(0,0,255),2)
            


            if cy1<(cy+offset) and cy1 > (cy-offset):
                vh_down[id]=time.time()
            if id in vh_down:
                if cy2<(cy+offset) and cy2 > (cy-offset):
                    elapsed_time=time.time() - vh_down[id]
                    if counter.count(id)==0:
                        counter.append(id)
                        total_cars_detected += 1
                        distance = 10 # meters
                        a_speed_ms = distance / elapsed_time
                        a_speed_kh = a_speed_ms * 3.6
                        speeds.append(a_speed_kh)
                        cv2.circle(frame,(cx,cy),4,(0,0,255),-1)
                        cv2.putText(frame,str(id),(x3,y3),cv2.FONT_HERSHEY_COMPLEX,0.6,(255,255,255),1)
                        cv2.putText(frame,str(int(a_speed_kh))+'Km/h',(x4,y4 ),cv2.FONT_HERSHEY_COMPLEX,0.8,(0,255,255),2)

            

        cv2.line(frame,(274,cy1),(814,cy1),(255,255,255),1)

        cv2.putText(frame,('L1'),(277,320),cv2.FONT_HERSHEY_COMPLEX,0.8,(0,255,255),2)


        cv2.line(frame,(177,cy2),(927,cy2),(255,255,255),1)
    
        cv2.putText(frame,('L2'),(182,367),cv2.FONT_HERSHEY_COMPLEX,0.8,(0,255,255),2)
        d=(len(counter))

        cv2.imshow("RGB", frame)
        if cv2.waitKey(1)&0xFF==27:
            break
    cap.release()
    cv2.destroyAllWindows()

def main():
    while True:
        detect_speed()
        speed = 0
        for s in speeds:
            speed += s
        avg_speed = speed/len(speeds)
        time_est = road_length/avg_speed
        send_data_to_server(road_name,camera_id,str(time_est))
        time.sleep(PUBLISH_INTERVAL)
        speeds.clear()


if __name__ == "__main__":
    main()

