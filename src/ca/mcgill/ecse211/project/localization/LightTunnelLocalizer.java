package ca.mcgill.ecse211.project.localization;

import static ca.mcgill.ecse211.project.game.Resources.*;
import static ca.mcgill.ecse211.project.game.WifiResources.*;
import ca.mcgill.ecse211.project.sensor.LightUser;
import static ca.mcgill.ecse211.project.game.Helper.*;

/**
 * This class contains methods for localization before and after traversing a tunnel
 * @author Liang Zhao & Jessie Tang
 *
 */
public class LightTunnelLocalizer implements LightUser {
  private boolean localizing = false;
  private double[] initialLight = new double[2];
  private volatile boolean gotInitialSample = false;
  private volatile int step;
  private volatile double[] offset = new double[2];
  public volatile boolean isRight;
  private volatile boolean before;
  public double backedupDist;

  /**
   * Method to begin tunnel before/after localization
   */
  public void localize() {
    // get yDistFromLine and adjust heading after localization
    localizing = true;
    step = 0;
    while (localizing);
    
    double tunnelTurn = calcTurnThetaDeg();
    if (isRight()) {
      turnRight(tunnelTurn);
    } else {
      turnLeft(tunnelTurn);
    }
  }
  
  /**
   * Method to calculate turn angle
   * @return
   */
  public double calcTurnThetaDeg() {
    double turnTheta = Math.atan(Math.abs((offset[1] - offset[0])) / WHEEL_BASE);
    double turnThetaDeg = 180 * turnTheta / Math.PI;
    return turnThetaDeg;
  }
  
  /**
   * returns a boolean isRight
   * @return if isRight is true, the robot turn right the calculated turn theta, else turns left
   */
  public boolean isRight() {
    return isRight;
  }
  
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
      moveBackward(TILE_SIZE/2);
      setLRMotorSpeed(LS_TUNNEL_SPEED);
      moveForward(5);
      initialLight[0] = light[0];
      initialLight[1] = light[1];
 
      moveForward();
      sleepFor(50);
      
      gotInitialSample = true; 
    }
    
    //left sensor sees line and right doesn't
    else if (light[0]/initialLight[0] < LIGHT_THRESHOLD_L && light[1]/initialLight[1] > LIGHT_THRESHOLD_R) {
      switch(step) {
        case 0:
          stopMotors();
          isRight = false;
          offset[0] = odometer.getXYT()[1];
          moveForward();
          sleepFor(150);
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
    
    //right sensor sees line and left doesn't
    else if (light[0]/initialLight[0] > LIGHT_THRESHOLD_L && light[1]/initialLight[1] < LIGHT_THRESHOLD_R) {
      switch (step) {
        case 0:
          stopMotors();
          isRight = true;
          offset[0] = odometer.getXYT()[1];
          moveForward();
          sleepFor(150);
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
