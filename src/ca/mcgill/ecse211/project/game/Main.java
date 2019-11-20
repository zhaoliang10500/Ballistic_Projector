package ca.mcgill.ecse211.project.game;

import lejos.robotics.SampleProvider;
import lejos.hardware.Button;
import static ca.mcgill.ecse211.project.game.Resources.*;
import ca.mcgill.ecse211.project.localization.*;
import ca.mcgill.ecse211.project.sensor.*;
import ca.mcgill.ecse211.project.odometry.*;

/**
 * This class contains the main method of the program.
 * This program allows an EV3 robot to autonomously localize and navigate a course (traversing a tunnel and avoiding obstacles)
 * The robot then navigates to a specified launch point and throws 5 balls at a target
 *
 */
public class Main {
  //TODO: ReadMe in documentation
  //TODO: record videos of robot for presentation
  //TODO: debreif document put questions in, record data of sucess/failure during competition
  
  
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
    
    //synchronized method to control sensor threads
    SensorController sensorControl = SensorController.getSensorController(USPoll, lightPoll);
    
    USLocalizer USLoc = new USLocalizer();
    LightLocalizer lightLoc = new LightLocalizer();
    LightTunnelLocalizer lightTunnelLoc1 = new LightTunnelLocalizer();
    LightTunnelLocalizer lightTunnelLoc2 = new LightTunnelLocalizer();
    LightTunnelLocalizer lightTunnelLoc3 = new LightTunnelLocalizer();
    LightTunnelLocalizer lightTunnelLoc4 = new LightTunnelLocalizer();
    ObstacleAvoidance obAvoid1 = new ObstacleAvoidance();
    ObstacleAvoidance obAvoid2 = new ObstacleAvoidance();
    
    GameController gameControl = new GameController(sensorControl, USLoc, lightLoc, lightTunnelLoc1, lightTunnelLoc2, lightTunnelLoc3, lightTunnelLoc4, obAvoid1, obAvoid2);
    //TODO: obstacle avoidance might not work this way
    
    Thread odoThread = new Thread(odometer); //odometer created in Resources
    Thread USThread = new Thread(USPoll);
    Thread lightThread = new Thread(lightPoll);
    //TODO: might have to implement odometry correction inside odometer, currently it is separate
    Thread gameThread = new Thread(gameControl);
    
    //Get parameters from WiFi class
    //Server file included now in project, cd to the jar (java -jar EV3WifiServer.jar)
    //Make sure to change the SERVER_IP in WifiResources to your that of your computer (hostname -I)
//    WiFi.wifi();
//    
//    if (WiFi.recievedParameters) {
//      LCD.clear();
//      odoThread.start();
//      USThread.start();
//      lightThread.start();
//      gameThread.start();
//    }
    
    WiFi.wifi();
    
//    LCD.clear();
//    odoThread.start();
//    USThread.start();
//    lightThread.start();
//    gameThread.start();
    
    while (Button.waitForAnyPress() != Button.ID_ESCAPE);
    System.exit(0);
  }
  
}