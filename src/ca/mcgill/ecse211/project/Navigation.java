package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import static ca.mcgill.ecse211.project.Helper.*;

import java.util.concurrent.CountDownLatch;


public class Navigation extends Thread{
  private static double x, y; 
  private static double deltaX, deltaY;
  
  private static double minDist, travelDist;
  private static double theta1, theta2;
  public static double turnedAngle; //for calculating launch (for display)
  
  public double[] target;
  public double[] launch;
  
  private CountDownLatch latch2;
  
  /**
   * Class constructor
   */
  public Navigation (CountDownLatch latch2) {
    launch = new double[2];
    target = new double[] {5,6};
    this.latch2 = latch2;
    
    // Reset motors, navigating, and set odometer 
    leftMotor.stop();
    rightMotor.stop();
    odometer.setXYT(TILE_SIZE, TILE_SIZE, 0);
  }

  
  /**
   * Moves the robot to each desired waypoint
   * @param xCoord  x coordinate of waypoint[i]
   * @param yCoord  y coordinate of waypoint[i]
   */
  public void run () { //travelTo
    try {
      latch2.await();
    } catch(InterruptedException e) {
      LCD.drawString("Interrupted Exception", 0, 0);
    }
    
    double[] xyCoord = target;
    
    LCD.drawString("Target: " + target, 0, 4);
    // Gets current x, y positions (already in cm) 
    x = odometer.getXYT()[0];
    y = odometer.getXYT()[1];
    
    deltaX = TILE_SIZE*xyCoord[0] - x;  
    deltaY = TILE_SIZE*xyCoord[1] - y;
    minDist = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
    travelDist = minDist - NAV_OFFSET;
    
    // Turn
    theta2 = Math.toDegrees(Math.atan2(deltaX, deltaY)); //theta2 now in degrees
    theta1 = odometer.getXYT()[2]; // theta1 in degrees
    
    turnTo(theta2 - theta1);
    
    rightMotor.setSpeed(NAV_FORWARD);
    leftMotor.setSpeed(NAV_FORWARD);
    
    
    
    // Calculate launch coordinates for display
    launch[0] = travelDist*Math.cos(turnedAngle);
    launch[1] = travelDist*Math.sin(turnedAngle);
    LCD.drawString("Lauch: " + launch, 0, 5);
    
    // Move
    leftMotor.rotate(Helper.convertDistance(travelDist, WHEEL_RADIUS), true);
    rightMotor.rotate(Helper.convertDistance(travelDist, WHEEL_RADIUS), false);

  }
  
  /**
   * Causes the robot to turn (on point) to the absolute heading theta. 
   * This method should turn a MINIMAL angle to its target. 
   * @param theta  robot turning angle before each waypoint
   */
  private void turnTo(double theta) {
    if (theta > 180) {
      theta = 360 - theta;
    }
    else if (theta < -180) {
      theta = 360 + theta;
    }
    
    turnedAngle = theta;
    
    leftMotor.setSpeed(NAV_ROTATE);
    rightMotor.setSpeed(NAV_ROTATE);
    leftMotor.rotate(convertAngle(theta, WHEEL_RADIUS), true);
    rightMotor.rotate(-convertAngle(theta, WHEEL_RADIUS), false);

  }
  
  
}