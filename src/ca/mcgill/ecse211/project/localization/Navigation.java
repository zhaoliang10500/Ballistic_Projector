package ca.mcgill.ecse211.project.localization;

import static ca.mcgill.ecse211.project.game.Resources.*;
import static ca.mcgill.ecse211.project.game.Helper.*;
import ca.mcgill.ecse211.project.game.Helper;

/**
 * This class contains methods for navigations
 * Everything here should be static
 */
public class Navigation {
  private static double x, y; 
  private static double deltaX, deltaY;

  private static double minDist, travelDist;
  private static double theta1, theta2;

  public static double[] launch;


  public static void travelTo (double[] target) { //travelTo 
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

    //Calculate angles, atan = first/second
    theta2 = Math.toDegrees(Math.atan2(deltaX, deltaY)); //theta2 now in degrees
    theta1 = odometer.getXYT()[2]; // theta1 in degrees

    // Gets current x, y positions (already in cm) 
    x = odometer.getXYT()[0];
    y = odometer.getXYT()[1];

    deltaX = x - TILE_SIZE*xyCoord[0] + TILE_SIZE/2;
    deltaY = TILE_SIZE*xyCoord[1] - y + TILE_SIZE/2;

    //Turn
    turnTo(theta2 - theta1);


    // Move 
    minDist = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
    travelDist = minDist - NAV_OFFSET;
    leftMotor.rotate(Helper.convertDistance(travelDist, WHEEL_RADIUS), true);
    rightMotor.rotate(Helper.convertDistance(travelDist, WHEEL_RADIUS), false);
  }

  /**
   * Causes the robot to turn (on point) to the absolute heading theta. 
   * This method should turn a MINIMAL angle to its target. 
   * @param theta  robot turning angle before each waypoint
   */
  private static void turnTo(double theta) {
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

    // Calculate launch coordinates for display
    launch[0] = travelDist*Math.sin(Math.toRadians(turnAngle)) + TILE_SIZE; //x
    launch[1] = travelDist*Math.cos(Math.toRadians(turnAngle)) + TILE_SIZE; //y

    LCD.drawString("Lauch: " + (int)launch[0] + ", " + (int)launch[1], 0, 5);

    leftMotor.setSpeed(NAV_ROTATE);
    rightMotor.setSpeed(NAV_ROTATE);


    leftMotor.rotate(convertAngle(turnAngle, WHEEL_RADIUS), true);
    rightMotor.rotate(-convertAngle(turnAngle, WHEEL_RADIUS), false);
  }


}