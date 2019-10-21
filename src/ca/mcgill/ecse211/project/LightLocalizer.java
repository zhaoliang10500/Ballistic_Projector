package ca.mcgill.ecse211.project;
import static ca.mcgill.ecse211.project.Resources.*;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;
import static ca.mcgill.ecse211.project.Helper.*;

import java.util.concurrent.CountDownLatch;

/**
 * Light sensor localization class
 */
public class LightLocalizer extends Thread {
  private double intensity, initialReading;
  private final long waitTime = 100;
  private double[] angles; //angles recorded at the 1-4 lines
  
  private SampleProvider lsSampleProvider;
  private float[] lsData;
  
  private CountDownLatch latch, latch2;
  
  
  /**
   * Assumes the robot starts at theta = 0 in a position close enough to (1,1)
   * Where a 360 degree rotation allows the sensor to capture all four gridlines
   */
  public LightLocalizer(SampleProvider lsSampleProvider, float[] lsData, CountDownLatch latch, CountDownLatch latch2) {
    this.lsSampleProvider = lsSampleProvider;
    this.lsData = lsData;
    angles = new double[4];
    
    leftMotor.setSpeed(LS_SPEED);
    rightMotor.setSpeed(LS_SPEED);
    
    this.latch = latch;
    this.latch2 = latch2;
  }
  
  /**
   * Runs the logic of the light localizer
   */
  public void run() {
    try {
      latch.await();
    } catch(InterruptedException e) {
      LCD.drawString("Interrupted Exception", 0, 0);
    }

    initialReading = meanFilter();
    
    // initial position adjustment to move closer to origin
    turnRight(90);
    moveBackward(4);
    forwardTillLine();
    moveForward(LS_DISTANCE/3);
    
    turnLeft(90);
    moveBackward(5);
    forwardTillLine();
    moveForward(1.1*LS_DISTANCE/2);
    odometer.setTheta(0);
    
    // Localize
    find4Angles();
    
    //calculate x and y offsets from origin
    double thetaY = angles[2] - angles[0];
    double xError = 35; //the measured xTheta tend to be too large due to LS inaccuracies
    double thetaX = angles[0] + angles[1] + (360-angles[3]-xError);
    
    double dy = LS_DISTANCE*Math.cos(thetaX/2*Math.PI/180);
    double dx = LS_DISTANCE*Math.cos(thetaY/2*Math.PI/180);
    
    odometer.setY(-dy + TILE_SIZE);
    odometer.setX(-dx + TILE_SIZE );
    
    // Move robot's center of rotation to (1,1) and turn to 0 degrees
    turnRight(90);
    moveForward(dx);
    turnLeft(90);
    moveForward(dy);
    latch2.countDown();
  }
  
  /**
   * Method to find the 4 angles relative to the grid lines
   */
  private void find4Angles() {
    // both motors non-blocking to allow gridline detection while turning
    leftMotor.rotate(convertAngle(360, WHEEL_RADIUS), true);
    rightMotor.rotate(-convertAngle(360, WHEEL_RADIUS), true);
    
    //robot turns clockwise, records 4 angles  
    for (int i = 0; i < angles.length; i++) {
      angles[i] = recordAngle();
      try {
        Thread.sleep(waitTime);
      } catch(InterruptedException e) {
        LCD.drawString("Interrupted Exception", 0, 0);
      }
    }
    
    // Wait for robot to finish turning
    while (leftMotor.isMoving() || rightMotor.isMoving()) {
      // wait
      try {
        Thread.sleep(50);
      } catch (Exception e) {
        // nothing
      }

    }
  }
  
  /**
   * Filter to obtain the average of MEAN_FILTER samples
   * @return  the average of MEAN_FILTER number of samples
   */
  private double meanFilter () {
    int sum = 0;
    for (int i = 0; i < MEAN_FILTER; i++) {
      lsSampleProvider.fetchSample(lsData, 0);
      sum += lsData[0]*100;
    }
    return sum/MEAN_FILTER; //return median
  }
  
  /**
   * Get the odometer angle at each grid line
   * @return  odometer angle at each grid line
   */
  private double recordAngle() {
    while(leftMotor.isMoving() || rightMotor.isMoving()) {
      intensity = meanFilter();
      if (intensity/initialReading < INTENSITY_THRESHOLD) {
        Sound.beep();
        return odometer.getXYT()[2]; //get the angle at a line
      }
    }
    return -1;
  }
  
  /**
   * Moves robot forward until a grid line is seen
   */
  private void forwardTillLine() {
    while (true) {
      intensity = meanFilter();
      // line detected
      if (intensity/initialReading < INTENSITY_THRESHOLD) {
         Sound.beep();
         break;
      }
      moveForward(); // move forward until line detected
    } 
  }
  
}