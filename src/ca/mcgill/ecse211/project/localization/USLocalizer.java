package ca.mcgill.ecse211.project.localization;
import static ca.mcgill.ecse211.project.game.Helper.*;
import static ca.mcgill.ecse211.project.game.Resources.*;
import ca.mcgill.ecse211.project.sensor.*;
import java.util.Arrays;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

/**
 * This class contains methods for ultrasonic localization
 *
 */
public class USLocalizer implements USUser { 
  private boolean localizing = false;
  private int step;
  private double theta1, theta2;
  private double dTheta;
  private int edgeThreshold = 40; // distance threshold for deciding edge type
  private volatile int initialDistance;
  private volatile EdgeType USLocType;
  private volatile boolean gotInitialSample = false;


  private enum EdgeType {
    RISING_EDGE,
    FALLING_EDGE
  }

  /**
   * Method to begin US localization
   */
  public void localize() { 

    localizing = true;
    step = 0;

    while(localizing); //don't continue program until localizing = false
    
    if (USLocType == EdgeType.FALLING_EDGE) {
      dTheta = FALL_ANGLE + (theta1 + theta2)/2;
      //turnLeft(dTheta); turn to 0 
      turnLeft(dTheta - 90); //turn to 90
    }
    else {
      dTheta = RISE_ANGLE - (theta1 + theta2)/2;
      //turnRight(dTheta - 90); turn to 0 
      turnRight(dTheta); //turn to 90
    }
    odometer.setTheta(90);
  }


  /**
   * Method to process US poller data
   * Uses falling edge localization (facing away from wall)
   * Implemented from interface USUser
   */
  @Override
  public void processUSData(int distance) {
    if (!localizing) {
      return;
    }
    else if (!gotInitialSample) {
      initialDistance = distance;
      if (initialDistance < edgeThreshold) {
        USLocType = EdgeType.RISING_EDGE;
      }
      else {
        USLocType = EdgeType.FALLING_EDGE;
      }
      turnLeft();
      gotInitialSample = true;
    }
    else if (gotInitialSample && USLocType == EdgeType.FALLING_EDGE) { //facing away from wall
      if (distance > FALL_THRESHOLD){
        switch(step) {
          case 0:
            step++;
            break;
          case 2:
            step++;
            break;
        }
      }
      else if (distance < FALL_THRESHOLD) {
        switch(step) {
          case 1:
            stopMotors();
            theta1 = 360 - odometer.getXYT()[2]; 
            turnRight();
            sleepFor(100);
            step++;
            break;
          case 3:
            theta2 = odometer.getXYT()[2];    
            stopMotors();
            step++;
            localizing = false;
            break;
        }
      }
    }
    else if (gotInitialSample && USLocType == EdgeType.RISING_EDGE) { //facing towards wall
      if (distance < RISE_THRESHOLD){
        switch(step) {
          case 0:
            step++;
            break;
          case 2:
            step++;
            break;
        }
      }
      else if (distance > RISE_THRESHOLD) {
        switch(step) {
          case 1:
            stopMotors();
            theta1 = 360 - odometer.getXYT()[2]; 
            turnRight();
            sleepFor(100);
            step++;
            break;
          case 3:
            theta2 = odometer.getXYT()[2];    
            stopMotors();
            step++;
            localizing = false;
            break;
        }
      }
      
    }

  }



}