package ca.mcgill.ecse211.project.localization;
import static ca.mcgill.ecse211.project.game.Helper.*;
import static ca.mcgill.ecse211.project.game.Resources.*;
import ca.mcgill.ecse211.project.sensor.*;
import java.util.Arrays;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;

public class USLocalizer implements USUser {
  private int edgeType; 
  private SampleProvider usSampleProvider;
  private float[] usData;
  private int distance;
  private boolean localizing = false;
  
  /**
   * Runs the logic of the US localizer
   */
  public void localize() { 
    double dTheta;
    if (edgeType == RISING) {
      dTheta = risingEdge(); 
    }
    else if (edgeType == FALLING) {
      dTheta = fallingEdge();
    }
    else {
      dTheta = -1;
    }
    
    odometer.setTheta(dTheta);
    
    // physically turn robot to a heading of 0 degrees 
    turnLeft(dTheta);
  }
  
  
  /**
   * Used when robot is facing towards the wall. 
   * The robot turns till it no longer see the wall (rising edge) and repeats on the other side
   * @return
   */
  private double risingEdge() {
    double theta1, theta2; //angles of first and second rising edge
    while (this.distance < RISE_THRESHOLD) {
      turnRight();
    }
    
    Sound.beep();
    stopMotors();
    
    theta1 = odometer.getXYT()[2];
    
    turnLeft(40);
    
    while(this.distance < RISE_THRESHOLD) {
      turnLeft();
    }
    
    Sound.beep();
    stopMotors();
    theta2 = 360 - odometer.getXYT()[2];
    
    return RISE_ANGLE - (theta1 + theta2)/2;
  }
  
  /**
   * Used when robot is facing way from wall.
   * The robot turns till it sees the wall (falling edge) and repeats on the other side
   * @return
   */
  private double fallingEdge() {
    double theta1, theta2; //angles of first and second rising edge
    while (this.distance > FALL_THRESHOLD) {
      turnLeft();
    }
    
    Sound.beep();
    stopMotors();
    
    theta1 = 360 - odometer.getXYT()[2];
    
    turnRight(40);
    
    while(this.distance > FALL_THRESHOLD) {
      turnRight();
    }
    
    Sound.beep();
    stopMotors();
    theta2 = odometer.getXYT()[2];
    
    return FALL_ANGLE + (theta1 + theta2)/2;
  }
  
  
  /**
   * Method to process us poller distance
   * Uses a median filter
   */
  @Override
  public void processUSData(int distance) {
    int[] tempData = new int[MEDIAN_FILTER];
    if (!localizing) {
      return;
    }
    else {
      for (int i = 0; i < MEDIAN_FILTER; i++) {
        tempData[i] = distance;
      }
      Arrays.sort(tempData);
      if (tempData[MEDIAN_FILTER/2] > 255) {
        this.distance = 255;
      }
      this.distance = tempData[MEDIAN_FILTER/2]; 
    }
    
  }
  
  
  
}