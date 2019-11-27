package ca.mcgill.ecse211.project.game;

import lejos.robotics.SampleProvider;
import lejos.hardware.Button;
import static ca.mcgill.ecse211.project.game.Resources.*;
import ca.mcgill.ecse211.project.localization.*;
import ca.mcgill.ecse211.project.odometry.*;
import static ca.mcgill.ecse211.project.game.WifiResources.GOT_WIFI_PARAMS;

/**
 * This class contains the main method of the program.
 * This program allows an EV3 robot to autonomously localize and navigate a course (traversing a tunnel and avoiding obstacles)
 * The robot then navigates to a specified launch point and throws 5 balls at a target
 *
 */
public class Main {
  static boolean doneProgram = false;
  static int buttonChoice = Button.ID_DOWN;

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

    Odometer odometer = Odometer.getOdometer();
    odometer.start();

    USLocalizer USLoc = new USLocalizer(leftMotor, rightMotor, odometer, usSamp, usData);
    LightLocalizer lightLoc = new LightLocalizer(odometer, lightSampL, lightDataL, lightSampR, lightDataR);
    LightTunnelLocalizer lightTunnelLoc = new LightTunnelLocalizer(odometer, lightSampL, lightDataL, lightSampR, lightDataR);
    ObstacleAvoidance obstacleAvoidance = new ObstacleAvoidance(odometer, leftMotor, rightMotor, USMotor);
    //Get parameters from WiFi class
    //Server file included now in project, cd to the jar (java -jar EV3WifiServer.jar)
    //Make sure to change the SERVER_IP in WifiResources to your that of your computer (hostname -I)
    WiFi.wifi(); //inside WiFi, sleeps for 2sec to wait for above threads to start


    if (GOT_WIFI_PARAMS) {
//        //US localization
//        setLRMotorSpeed(US_SPEED);
//        USLoc.doLocalization();
//
//        // light localization
//        //sleepFor(2000);
//        lightLoc.localize();
//        GameController.beep(3);
//
//        //Travel to tunnel and face it
//        GameController.travelToTunnel(GameController.calcTunnelCoords(), WiFi.CORNER, false, false);
//        GameController.straighten(WiFi.CORNER);

        // light localization before tunnel
//        setLRMotorSpeed(LS_TUNNEL_SPEED);
//        lightTunnelLoc.localize(); 

//        // travel through tunnel
//        setLRMotorSpeed(TUNNEL_SPEED);
//        Navigation.travelThroughTunnel();
//
//        // light localization after tunnel
//        setLRMotorSpeed(LS_TUNNEL_SPEED);
//        lightTunnelLoc.localize(); 
//
        // navigation: travel to launch point
        obstacleAvoidance.travelTo(WiFi.BIN.x, WiFi.BIN.y, 0, true);
        GameController.beep(3);
//
//       GameController.beep(3);
//
//
//        // throw balls
//        for (int i = 0; i<5; i++) {
//          Launcher.launch();
//        }
//
//        sleepFor(25000); //wait till finished throw
//
//        // travel back to tunnel and face it
//        int currCorner = GameController.calcCurrCorner();
//        GameController.travelToTunnel(GameController.calcTunnelCoords(), currCorner, false, true);
//        GameController.straighten(currCorner);
//
//        // light localization before tunnel
//        setLRMotorSpeed(LS_TUNNEL_SPEED);
//        //sleepFor(2000);
//        lightTunnelLoc.localize();
//
//        // travel through tunnel
//        setLRMotorSpeed(TUNNEL_SPEED);
//        Navigation.travelThroughTunnel();

//        // light localization after tunnel
//        setLRMotorSpeed(LS_TUNNEL_SPEED);
//        sleepFor(2000);
//        lightTunnelLoc.localize();

        // travel back to starting position
//        double[] initialXY = GameController.calcInitialPos();
//        Navigation.travelTo(initialXY[0], initialXY[1], 0, false);
//        GameController.beep(5);
//        
        //while(Button.waitForAnyPress() != Button.ID_ESCAPE);
        //System.exit(0);
      }
  }
}
