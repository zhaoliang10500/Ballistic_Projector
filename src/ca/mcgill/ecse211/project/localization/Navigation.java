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

  private static double minDist;
  private static double theta1, theta2;

  public static double[] launch;
  
  public static double turnAngle;


  public static void travelTo (double xCoord, double yCoord, double angleOffset) { //travelTo 
    double[] xyCoord = {xCoord, yCoord};
    
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

    //Turn
    turnAngle = calcTurnAngle(theta2 - theta1, angleOffset);
    turnTo(turnAngle);


    // Move 
    minDist = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
    setLRMotorSpeed(NAV_FORWARD);
    //no NAV_OFFSET this time b/c robot destination coordinates were directly given 
    leftMotor.rotate(Helper.convertDistance(minDist, WHEEL_RADIUS), true);
    rightMotor.rotate(Helper.convertDistance(minDist, WHEEL_RADIUS), false);
    
  }
  
  /**
   * Calculates and returns the minimal turn angle to face a given angle theta
   * @param theta
   * @return
   */
  public static double calcTurnAngle(double theta, double angleOffset) {
    if (theta > 180) {
      theta = 360 - theta;
    }
    else if (theta < -180) {
      theta = 360 + theta;
    }
    //else theta = theta
    return theta + angleOffset; //to compensate for overturning in navigation
  }
  
  /**
   * Method to turn the robot theta degrees
   * @param theta
   */
  public static void turnTo(double theta) {
    setLRMotorSpeed(NAV_TURN);

    leftMotor.rotate(convertAngle(theta, WHEEL_RADIUS), true);
    rightMotor.rotate(-convertAngle(theta, WHEEL_RADIUS), false);
    
  }
  
  /**
   * Method to travel through a tunnel
   * The robot must begin with its center of rotation (between wheels) 
   * one tile away from the entrance of the tunnel
   * Four tile sizes are traveled and the robot ends up with its center of rotation 
   * one tile size away from the tunnel exit
   */
  public static void travelThroughTunnel() {
    moveForward(TILE_SIZE*4);
  }


}