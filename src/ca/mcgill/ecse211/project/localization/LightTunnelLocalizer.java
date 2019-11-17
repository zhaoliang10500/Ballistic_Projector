package ca.mcgill.ecse211.project.localization;

import static ca.mcgill.ecse211.project.game.Resources.*;
import static ca.mcgill.ecse211.project.game.WifiResources.*;
import ca.mcgill.ecse211.project.sensor.LightUser;
import static ca.mcgill.ecse211.project.game.Helper.*;

/**
 * This class contains methods for localization before and after traversing a tunnel
 *
 */
public class LightTunnelLocalizer implements LightUser {
  private boolean localizing = false;
  private double[] initialLight = new double[2];
  private volatile boolean gotInitialSample = false;
  private volatile int step;
  private volatile double[] offset = new double[2];
  private volatile boolean turnRight;
  private volatile boolean before;
  public double backedupDist;

  /**
   * Method to begin tunnel before/after localization
   * 
   * @param before boolean to specify whether the localization is happening before or after traversing a tunnel
   */
  public void localize(boolean before) {
    // sleepFor(500);
    this.before = before;

    // get yDistFromLine and adjust heading after localization
    localizing = true;
    step = 0;
    while (localizing);

    double yTheta = Math.atan((offset[1] - offset[0]) / WHEEL_BASE);
    double yThetaDeg = 180 * yTheta / Math.PI;

    if (turnRight) {
      turnRight(yThetaDeg);
    } else {
      turnLeft(yThetaDeg);
    }

    // backedupDist = odometer.getXYT()[1]; //used to make the localization work even if backed up one extra tile

    // moveBackward(yDistFromLine);
    double currentY = odometer.getXYT()[1];
    odometer.setY(currentY + CS_DISTANCE);
  }

  // TODO: fool proof tunnel localization
  // public double backupDist() {
  // return backedupDist;
  // }

  /**
   * Method to process light Poller data
   * Implemented from LightUser
   */
  @Override
  public void processLightData(int[] light) {
    if (!localizing) {
      return;
    }
    else if (!gotInitialSample) {
      setLRMotorSpeed(170);
      moveBackward(TILE_SIZE-3);
      setLRMotorSpeed(CS_TUNNEL_SPEED);
      moveForward(5);
      moveBackward(2);
      initialLight[0] = light[0];
      initialLight[1] = light[1];
      
//      if (before) {
//        moveBackward();
//      }
//      else {
//        moveForward();
//      }
      moveForward();
      sleepFor(50);
      
      gotInitialSample = true; 
    }
  //for y, left sensor sees line and right doesn't
    else if (light[0]/initialLight[0] < LIGHT_THRESHOLD_L && light[1]/initialLight[1] > LIGHT_THRESHOLD_R) {
      switch(step) {
        case 0:
          stopMotors();
          turnRight = false;
          offset[0] = odometer.getXYT()[1];
//          if (this.before) {
//            moveBackward();
//          }
//          else {
//            moveForward();
//          }
          moveForward();
          sleepFor(150);
          //sleepFor(50);
          step ++;
          break;
          
        case 1:
          stopMotors();
          offset[1] = odometer.getXYT()[1];
          step++;
          localizing = false;
          break;  
      }
    }
    //for x, right sensor sees line and left doesn't
    else if (light[0]/initialLight[0] > LIGHT_THRESHOLD_L && light[1]/initialLight[1] < LIGHT_THRESHOLD_R) {
      switch (step) {
        case 0:
          stopMotors();
          turnRight = true;
          offset[0] = odometer.getXYT()[1];
//          if (this.before) {
//            moveBackward();
//          }
//          else {
//            moveForward();
//          }
          moveForward();
          sleepFor(150);
          //sleepFor(50);
          step++;
          break;
          
        case 1:
          stopMotors();
          offset[1] = odometer.getXYT()[1];
          step++;
          localizing = false;
          break;  
      }
    }
    //both sensors see line at the same time
    else if (light[0]/initialLight[0] < LIGHT_THRESHOLD_L && light[1]/initialLight[1] < LIGHT_THRESHOLD_R) {
      stopMotors();
      offset[0] = 0;
      offset[1] = 0;
      localizing = false;
    }
  }
}