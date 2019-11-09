package ca.mcgill.ecse211.project.game;

import lejos.robotics.SampleProvider;
import lejos.hardware.Button;
import static ca.mcgill.ecse211.project.game.Resources.*;
import ca.mcgill.ecse211.project.localization.*;
import ca.mcgill.ecse211.project.sensor.*;
import ca.mcgill.ecse211.project.odometry.*;

/**
 * This class contains the main method of the program
 *
 */
public class Main {

  /**
   * Program entry point
   * @param args
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
    OdometryCorrection odoCorrect = new OdometryCorrection();
    ObstacleAvoidance obAvoid = new ObstacleAvoidance();
    //TODO: change Navigation class to suit current needs
    //Launcher is also completely static
    
    GameController gameControl = new GameController(sensorControl, USLoc, colorLoc, odoCorrect, obAvoid);
    //TODO: obstacle avoidance might not work this way
    
    Thread odoThread = new Thread(odometer); //odometer created in Resources
    Thread USThread = new Thread(USPoll);
    Thread colorThread = new Thread(colorPoll);
    //TODO: might have to implement odometry correction inside odometer, currently it is separate
    Thread gameThread = new Thread(gameControl);
    
    //Get parameters from WiFi class
    //WiFi.wifi();
    
    int buttonChoice;
    
    do {
      LCD.drawString("Press center", 0, 1);
      LCD.drawString("to begin", 0, 2);
      buttonChoice = Button.waitForAnyPress();
    } while (buttonChoice != Button.ID_ENTER && buttonChoice != Button.ID_ESCAPE);
    
    if (buttonChoice == Button.ID_ENTER) {
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