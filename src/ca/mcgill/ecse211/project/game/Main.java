package ca.mcgill.ecse211.project.game;

import lejos.robotics.SampleProvider;
import lejos.hardware.Button;

import static ca.mcgill.ecse211.project.game.Resources.*;
import ca.mcgill.ecse211.project.localization.*;
import ca.mcgill.ecse211.project.sensor.*;
import ca.mcgill.ecse211.project.odometry.*;
import static ca.mcgill.ecse211.project.game.WifiResources.GOT_WIFI_PARAMS;

/**
 * This class contains the main method of the program.
 * This program allows an EV3 robot to autonomously localize and navigate a course (traversing a tunnel and avoiding obstacles)
 * The robot then navigates to a specified launch point and throws 5 balls at a target
 * Liang Zhao & Jessie Tang
 *
 */
public class Main {
  /**
   * Program entry point
   * @param args WiFi input parameters
   */
  public static void main(String[] args) {
    SampleProvider usSamp = US_SENSOR.getMode("Distance");
    float[] usData = new float[usSamp.sampleSize()];
    
    SampleProvider lightSampL = LIGHT_SENSOR_L.getMode("Red");
    float[] lightDataL = new float[lightSampL.sampleSize()];
    
    SampleProvider lightSampR = LIGHT_SENSOR_R.getMode("Red");
    float[] lightDataR = new float[lightSampR.sampleSize()];
    
    USPoller USPoll = new USPoller(usSamp, usData);
    LightPoller lightPoll = new LightPoller(lightSampL, lightDataL, lightSampR, lightDataR);
    Odometer odometer = Odometer.getOdometer();
    odometer.start();
    
    //synchronized method to control sensor threads
    SensorController sensorControl = SensorController.getSensorController(USPoll, lightPoll);
    
    
    USLocalizer USLoc = new USLocalizer(leftMotor, rightMotor, odometer, usSamp, usData);
    LightLocalizer lightLoc = new LightLocalizer(odometer, lightSampL, lightDataL, lightSampR, lightDataR);   
    ObstacleAvoidance obAvoid = new ObstacleAvoidance(odometer, leftMotor, rightMotor, USMotor, usSamp, usData);
    LightTunnelLocalizer lightTunnelLoc = new LightTunnelLocalizer();
    
    GameController gameControl = new GameController(sensorControl, USLoc, lightLoc, lightTunnelLoc, obAvoid);
    
    Thread odoThread = new Thread(odometer); //odometer created in Resources
    Thread USThread = new Thread(USPoll);
    Thread lightThread = new Thread(lightPoll);
    Thread gameThread = new Thread(gameControl);
    
    //start threads
    odoThread.start();
    USThread.start();
    lightThread.start();
    
    //Get parameters from WiFi class
    //Server file included now in project, cd to the jar (java -jar EV3WifiServer.jar)
    //Make sure to change the SERVER_IP in WifiResources to your that of your computer (hostname -I)
    WiFi.wifi(); //inside WiFi, sleeps for 2sec to wait for above threads to start
    
    if (GOT_WIFI_PARAMS) {
      gameThread.start();
    }
    
    while (Button.waitForAnyPress() != Button.ID_ESCAPE);
    System.exit(0);
  }
  
}
