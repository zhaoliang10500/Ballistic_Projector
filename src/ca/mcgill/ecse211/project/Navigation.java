package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;
import static ca.mcgill.ecse211.project.Helper.*;

import java.util.concurrent.CountDownLatch;
import lejos.hardware.Sound;


public class Navigation extends Thread{
  private static double x, y; 
  private static double deltaX, deltaY;
  
  private static double minDist, travelDist;
  private static double theta1, theta2;
  private boolean tooClose; //for calculating ideal launch
  
  public double[] target;
  public double[] launch;
  
  private CountDownLatch latch2;
  
  /**
   * Class constructor
   */
  public Navigation (CountDownLatch latch2) {
    launch = new double[2];
    target = new double[] {5,7}; //{3,3} {7,0} {7,5}
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
    rightMotor.setSpeed(NAV_FORWARD);
    leftMotor.setSpeed(NAV_FORWARD);
    
    LCD.drawString("Target: " + (int)target[0] + ", " + (int)target[1], 0, 4);
    // Gets current x, y positions (already in cm) 
    x = odometer.getXYT()[0];
    y = odometer.getXYT()[1];
    
    //TILE_SIZE/2 because true desired point at center of tile
    deltaX = TILE_SIZE*xyCoord[0] - x + TILE_SIZE/2;  
    deltaY = TILE_SIZE*xyCoord[1] - y + TILE_SIZE/2;
    
    minDist = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
    
    LCD.drawString("minDist" + minDist, 0, 7);
    
    if (minDist >= NAV_OFFSET) {
      tooClose = false;
      travelDist = minDist - NAV_OFFSET;
      
      //Calculate angles, atan = first/second
      theta2 = Math.toDegrees(Math.atan2(deltaX, deltaY)); //theta2 now in degrees
      theta1 = odometer.getXYT()[2]; // theta1 in degrees
    }
    else {
      tooClose = true;
      leftMotor.setSpeed(NAV_ROTATE);
      rightMotor.setSpeed(NAV_ROTATE);
      Helper.turnRight(89);
      leftMotor.setSpeed(NAV_FORWARD);
      rightMotor.setSpeed(NAV_FORWARD);
      Helper.moveForward(TILE_SIZE*6 + TILE_SIZE/1.3);
      leftMotor.setSpeed(NAV_ROTATE);
      rightMotor.setSpeed(NAV_ROTATE);
      Helper.turnLeft(89);
      Helper.moveBackward(TILE_SIZE/1.8);
      
      // Gets current x, y positions (already in cm) 
      x = odometer.getXYT()[0];
      y = odometer.getXYT()[1];
      
      deltaX = x - TILE_SIZE*xyCoord[0] + TILE_SIZE/2;
      deltaY = TILE_SIZE*xyCoord[1] - y + TILE_SIZE/2;
  
      minDist = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
      NAV_OFFSET = 5.8*TILE_SIZE; //because this case tend to be more inaccurate with localization
      travelDist = minDist - NAV_OFFSET;
      
      //Calculate angles, atan = first/second
      theta2 = Math.toDegrees(Math.atan2(deltaX, deltaY)); //theta2 now in degrees
      theta1 = odometer.getXYT()[2]; // theta1 in degrees
    }
    
    LCD.drawString("travelDist: " + travelDist, 0, 3);
    
    turnTo(theta2 - theta1);
    
    
    // Move forward
    leftMotor.rotate(Helper.convertDistance(travelDist, WHEEL_RADIUS), true);
    rightMotor.rotate(Helper.convertDistance(travelDist, WHEEL_RADIUS), false);
    
    Sound.buzz();
  }
  
  /**
   * Causes the robot to turn (on point) to the absolute heading theta. 
   * This method should turn a MINIMAL angle to its target. 
   * @param theta  robot turning angle before each waypoint
   */
  private void turnTo(double theta) {
    double turnAngle;
    if (theta > 180) {
      turnAngle = 360 - theta;
    }
    else if (theta < -180) {
      turnAngle = 360 + theta;
    }
    else {
      turnAngle = theta;
    }
    
    LCD.drawString("turnAngle: "+ turnAngle, 0, 6);
    // Calculate launch coordinates for display
    launch[0] = travelDist*Math.sin(Math.toRadians(turnAngle)) + TILE_SIZE; //x
    launch[1] = travelDist*Math.cos(Math.toRadians(turnAngle)) + TILE_SIZE; //y
    
    LCD.drawString("Lauch: " + (int)launch[0] + ", " + (int)launch[1], 0, 5);
    
    leftMotor.setSpeed(NAV_ROTATE);
    rightMotor.setSpeed(NAV_ROTATE);
    
    
    if (!tooClose) {
      leftMotor.rotate(convertAngle(turnAngle, WHEEL_RADIUS), true);
      rightMotor.rotate(-convertAngle(turnAngle, WHEEL_RADIUS), false);
    }
    else {
      leftMotor.rotate(-convertAngle(turnAngle, WHEEL_RADIUS), true);
      rightMotor.rotate(convertAngle(turnAngle, WHEEL_RADIUS), false);
    }

  }
  
  
}