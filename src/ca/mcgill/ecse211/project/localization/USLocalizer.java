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
  private int edgeThreshold = 50; // distance threshold for deciding edge type
  private volatile int initialDistance;
  private volatile int initialSamp1, initialSamp2;
  //private volatile int[] initialDists = new int[3];
  private volatile EdgeType USLocType;
  private volatile boolean gotInitialSample = false;
  private volatile boolean gotInitialSample1 = false;
  private volatile boolean gotInitialSample2 = false;
  //private volatile int initialSampleCounter = 3;

  private enum EdgeType {
    RISING_EDGE,
    FALLING_EDGE
  }

  /**
   * Method to begin US localization
   */
  public void localize() { 
    sleepFor(1000); //wait for odometer
    localizing = true;
    step = 0;
    USMotor.setSpeed(150);

    while(localizing); //don't continue program until localizing = false
    
    sleepFor(300);
    if (USLocType == EdgeType.FALLING_EDGE) {
      dTheta = FALL_ANGLE + (theta1 + theta2)/2;
      turnLeft(dTheta); //turn to 0 
      moveForward(2);
      turnRight(90); //turn to 90
    }
    else {
      dTheta = RISE_ANGLE - (theta1 + theta2)/2;
      turnRight(dTheta - 90); //turn to 0 
      moveForward(4);
      turnRight(90); //turn to 90
    }
    //odometer.setTheta(90);
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
      USMotor.rotate(-20);
      USMotor.stop();
      initialDistance = distance;
      USMotor.rotate(20);
      if (initialDistance < edgeThreshold) {
        USLocType = EdgeType.RISING_EDGE;
      }
      else {
        USLocType = EdgeType.FALLING_EDGE;
      }
      sleepFor(1500); //wait for other threads;
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
            sleepFor(300);
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
            sleepFor(300);
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