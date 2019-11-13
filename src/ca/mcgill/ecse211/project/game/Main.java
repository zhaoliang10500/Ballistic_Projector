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

  /**
   * Program entry point
   * @param args WiFi input parameters
   */
  public static void main(String[] args) {
    SampleProvider usSamp = US_SENSOR.getMode("Distance");
    float[] usData = new float[usSamp.sampleSize()];
    
    SampleProvider colorSampL = COLOR_SENSOR_L.getRGBMode();
    float[] colorDataL = new float[colorSampL.sampleSize()];
    
    SampleProvider colorSampR = COLOR_SENSOR_R.getRGBMode();
    float[] colorDataR = new float[colorSampR.sampleSize()];
    
    USPoller USPoll = new USPoller(usSamp, usData);
    ColorPoller colorPoll = new ColorPoller(colorSampL, colorDataL, colorSampR, colorDataR);
    
    //synchronized method to control sensor threads
    SensorController sensorControl = SensorController.getSensorController(USPoll, colorPoll);
    
    USLocalizer USLoc = new USLocalizer();
    ColorLocalizer colorLoc = new ColorLocalizer();
    ColorTunnelLocalizer colorTunnelLoc = new ColorTunnelLocalizer();
    OdometryCorrection odoCorrect = new OdometryCorrection();
    ObstacleAvoidance obAvoid = new ObstacleAvoidance();
    
    GameController gameControl = new GameController(sensorControl, USLoc, colorLoc, colorTunnelLoc, odoCorrect, obAvoid);
    //TODO: obstacle avoidance might not work this way
    
    Thread odoThread = new Thread(odometer); //odometer created in Resources
    Thread USThread = new Thread(USPoll);
    Thread colorThread = new Thread(colorPoll);
    //TODO: might have to implement odometry correction inside odometer, currently it is separate
    Thread gameThread = new Thread(gameControl);
    
    //Get parameters from WiFi class
    //Server file included now in project, cd to the jar (java -jar EV3WifiServer.jar)
    //Make sure to change the SERVER_IP in WifiResources to your that of your computer (hostname -I)
    WiFi.wifi();
    
    if (WiFi.recievedParameters) {
      LCD.clear();
      odoThread.start();
      USThread.start();
      colorThread.start();
      gameThread.start();
    }
    
    while (Button.waitForAnyPress() != Button.ID_ESCAPE);
    System.exit(0);
  }
  
}