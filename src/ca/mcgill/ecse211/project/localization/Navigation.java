package ca.mcgill.ecse211.project.localization;

import static ca.mcgill.ecse211.project.game.Resources.*;
import static ca.mcgill.ecse211.project.game.Helper.*;
import ca.mcgill.ecse211.project.game.Helper;
import ca.mcgill.ecse211.project.game.WiFi;
import lejos.hardware.Button;

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
  public static boolean doneNavTravel = false;
  public static boolean doneNavTurnFace = false;

  public static void travelTo (double xCoord, double yCoord, double angleOffset, boolean bin) { //travelTo 
    //do {
    double[] xyCoord = {xCoord, yCoord};
    sleepFor(1000);
    deltaX = TILE_SIZE*xyCoord[0] - odometer.getXYT()[0];  
    deltaY = TILE_SIZE*xyCoord[1] - odometer.getXYT()[1];

    //Calculate angles, atan = first/second
    theta2 = Math.toDegrees(Math.atan2(deltaX, deltaY)); //theta2 now in degrees
    theta1 = odometer.getXYT()[2]; // theta1 in degrees

    //Turn
    turnAngle = calcTurnAngle(theta2 - theta1, angleOffset);
    sleepFor(1000);
    turn(turnAngle);
    
    // Move 
    minDist = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
    setLRMotorSpeed(NAV_FORWARD);
    
    if (!bin) {
      sleepFor(1000);
      leftMotor.rotate(Helper.convertDistance(minDist, WHEEL_RADIUS), true);
      rightMotor.rotate(Helper.convertDistance(minDist, WHEEL_RADIUS), false);
    } 
    else if (bin) {
      sleepFor(1000);
      leftMotor.rotate(Helper.convertDistance(Math.abs(minDist - (LAUNCH_GRID_DIST*TILE_SIZE)), WHEEL_RADIUS), true);
      rightMotor.rotate(Helper.convertDistance(Math.abs(minDist- (LAUNCH_GRID_DIST*TILE_SIZE)), WHEEL_RADIUS), false);
    }
//    doneNavTravel = true;
//    
//    } while (Button.waitForAnyPress() != Button.ID_ESCAPE && doneNavTravel == false);
//    
//    if (Button.waitForAnyPress() == Button.ID_ESCAPE) {
//      System.exit(0);
//    }
    
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
    //else theta = theta/
    return theta + angleOffset; //to compensate for overturning in navigation
  }
  
  /**
   * Method to turn the robot theta degrees
   * +theta = right turn, -theta = left turn
   * @param theta
   */
  public static void turn(double theta) {
    setLRMotorSpeed(NAV_TURN);

    leftMotor.rotate(convertAngle(theta, WHEEL_RADIUS), true);
    rightMotor.rotate(-convertAngle(theta, WHEEL_RADIUS), false);
    
  }
  
  /**
   * Method to face the robot towards an input angle heading
   * @param heading
   */
  public static void turnToFace(int heading) {
    //do {
    double currTheta = odometer.getXYT()[2];
    double turnAngle = heading - currTheta;
   
    if (turnAngle < -180) {
      turnAngle = 360 + turnAngle;
    }
    else if (turnAngle > 180) {
      turnAngle = 360 - turnAngle;
    }
    
    //System.out.println("turntoface angle:" + turnAngle);
    turn(turnAngle);
//    doneNavTurnFace = true;
//    
//    } while (Button.waitForAnyPress() != Button.ID_ESCAPE && doneNavTurnFace == false);
//    
//    if (Button.waitForAnyPress() == Button.ID_ESCAPE) {
//      System.exit(0);
//    }
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