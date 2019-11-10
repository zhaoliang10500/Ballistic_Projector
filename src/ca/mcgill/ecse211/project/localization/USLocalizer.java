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
  
  
  /**
   * Method to begin US localization
   */
  public void localize() { 
    //wait for odometer thread to start
    sleepFor(2000);
    
    localizing = true;
    step = 0;
    
    turnLeft();
    
    while(localizing); //don't continue program until localizing = false
    
    dTheta = FALL_ANGLE + (theta1 + theta2)/2;
    odometer.setTheta(dTheta);
    
    // physically turn robot to a heading of 0 degrees 
    turnLeft(dTheta);
  }
  
  
  /**
   * Method to process US poller data
   * Uses falling edge localization (facing away from wall)
   */
  @Override
  public void processUSData(int distance) {
    if (!localizing) {
      return;
    }
    else if (distance > FALL_THRESHOLD){
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