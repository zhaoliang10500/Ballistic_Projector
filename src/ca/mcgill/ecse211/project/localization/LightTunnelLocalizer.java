package ca.mcgill.ecse211.project.localization;

import static ca.mcgill.ecse211.project.game.Resources.*;
import static ca.mcgill.ecse211.project.game.WifiResources.*;
import static ca.mcgill.ecse211.project.game.GameController.*;
import java.util.Arrays;
import ca.mcgill.ecse211.project.game.GameController;
import ca.mcgill.ecse211.project.odometry.Odometer;
import ca.mcgill.ecse211.project.sensor.LightUser;
import lejos.robotics.SampleProvider;
import static ca.mcgill.ecse211.project.game.Helper.*;

/**
 * This class contains methods for localization before and after traversing a tunnel
 *
 */
//public class LightTunnelLocalizer implements LightUser {
//  private boolean localizing = false;
//  private double[] initialLight = new double[2];
//  private volatile boolean gotInitialSample = false;
//  private volatile int step;
//  private volatile double[] offset = new double[2];
//  public volatile boolean turnRight;
//  private volatile boolean before;
//  public double backedupDist;
//
//  /**
//   * Method to begin tunnel before/after localization
//   * 
//   * @param 
//   */
//  public void localize() {
//    // get yDistFromLine and adjust heading after localization
//    localizing = true;
//    step = 0;
//    while (localizing);
//  }
//  
//  public double calcTurnThetaDeg() {
//    double turnTheta = Math.atan(Math.abs((offset[1] - offset[0])) / WHEEL_BASE);
//    double turnThetaDeg = 180 * turnTheta / Math.PI;
//    return turnThetaDeg;
//  }
//  
//  public boolean turnRight() {
//    return turnRight;
//  }
//  /**
//   * Method to process light Poller data
//   * Implemented from LightUser
//   */
//  @Override
//  public void processLightData(int[] light) {
//    if (!localizing) {
//      return;
//    }
//    else if (!gotInitialSample) {
//      setLRMotorSpeed(170);
//      moveBackward(TILE_SIZE/2);
//      setLRMotorSpeed(LS_TUNNEL_SPEED);
//      moveForward(5);
//      initialLight[0] = light[0];
//      initialLight[1] = light[1];
// 
//      moveForward();
//      sleepFor(50);
//      
//      gotInitialSample = true; 
//    }
//  //left sensor sees line and right doesn't
//    else if (light[0]/initialLight[0] < LIGHT_THRESHOLD_L && light[1]/initialLight[1] > LIGHT_THRESHOLD_R) {
//      switch(step) {
//        case 0:
//          stopMotors();
//          turnRight = false;
//          offset[0] = odometer.getXYT()[1];
//          moveForward();
//          sleepFor(150);
//          step ++;
//          break;
//          
//        case 1:
//          stopMotors();
//          offset[1] = odometer.getXYT()[1];
//          step++;
//          localizing = false;
//          break;  
//      }
//    }
//    //right sensor sees line and left doesn't
//    else if (light[0]/initialLight[0] > LIGHT_THRESHOLD_L && light[1]/initialLight[1] < LIGHT_THRESHOLD_R) {
//      switch (step) {
//        case 0:
//          stopMotors();
//          turnRight = true;
//          offset[0] = odometer.getXYT()[1];
//          moveForward();
//          sleepFor(150);
//          step++;
//          break;
//          
//        case 1:
//          stopMotors();
//          offset[1] = odometer.getXYT()[1];
//          step++;
//          localizing = false;
//          break;  
//      }
//    }
//    //both sensors see line at the same time
//    else if (light[0]/initialLight[0] < LIGHT_THRESHOLD_L && light[1]/initialLight[1] < LIGHT_THRESHOLD_R) {
//      stopMotors();
//      offset[0] = 0;
//      offset[1] = 0;
//      localizing = false;
//    }
//  }
//}

public class LightTunnelLocalizer{
  private float[][] lightData;
  private SampleProvider[] sampleProvider;
  private Odometer odometer;
  private int filterSize = 5;
  private int[][] tempLights = new int[2][filterSize];
  private boolean gotInitialSample = false;
  private double[] initialLight = new double[2];
  private double[] offset = new double[2];
  private boolean shouldRight;
  private boolean isLeftSensor;
  private int localizingAxis = -1;
  boolean aligned = false;
  
  public LightTunnelLocalizer(Odometer odometer, SampleProvider leftSamp, float[] leftLightData, SampleProvider rightSamp, float[] rightLightData) {
    this.sampleProvider = new SampleProvider[] {leftSamp, rightSamp};
    this.lightData = new float[][] {leftLightData, rightLightData};  
    this.odometer = odometer;
  }
  
  private double[] meanFilter() {
    double[] lights = new double[2];
    for (int i = 0; i<filterSize; i++) {
      //left sensor
      sampleProvider[0].fetchSample(lightData[0], 0); 
      tempLights[0][i] = (int)(lightData[0][0]*1000.0); 

      //right sensor
      sampleProvider[1].fetchSample(lightData[1], 0); 
      tempLights[1][i] = (int)(lightData[1][0]*1000.0);   
    }
    
    Arrays.sort(tempLights[0]);
    Arrays.sort(tempLights[1]);
    lights[0] = tempLights[0][filterSize/2]; //java rounds down for int division
    lights[1] = tempLights[1][filterSize/2];
    
    return lights;
  }
  
  public void localize() {
    if (!gotInitialSample) {
      moveBackward(TILE_SIZE/2);
      moveForward(4);
      initialLight = meanFilter();
      gotInitialSample = true;
    }
    
    int tunnelOri = GameController.tunnelOrientation();
    if (tunnelOri == 1) { //vertical
      localizingAxis = 1; //y
    } else if (tunnelOri == 2) { //horizontal
      localizingAxis = 0; //x
    }
    
    while (meanFilter()[0]/initialLight[0] > LIGHT_THRESHOLD_L && meanFilter()[1]/initialLight[1] > LIGHT_THRESHOLD_R) {
      moveForward();
    }
    stopMotors();
    
    if (meanFilter()[0]/initialLight[0] < LIGHT_THRESHOLD_L && meanFilter()[1]/initialLight[1] > LIGHT_THRESHOLD_R) { //left first
      shouldRight = false;
      offset[0] = odometer.getXYT()[localizingAxis];
      isLeftSensor = true;
    }
    else if (meanFilter()[0]/initialLight[0] > LIGHT_THRESHOLD_L && meanFilter()[1]/initialLight[1] < LIGHT_THRESHOLD_R) { //right first
      shouldRight = true;
      offset[0] = odometer.getXYT()[localizingAxis];
      isLeftSensor = false;
    }
    else {
      offset[0] = 0;
      offset[1] = 0;
      aligned = true;
      shouldRight = false; //this one doesn't matter, angle will be 0 anyway, for completness only
    }
    
    if (!aligned) {
      if (isLeftSensor) { //left saw line first, now looking for right
        while (meanFilter()[1]/initialLight[1] > LIGHT_THRESHOLD_R) { //now looking for right sensor line
          moveForward();
        }
        stopMotors();
      }
      else { //right saw line first, now looking for left
        while (meanFilter()[0]/initialLight[0] > LIGHT_THRESHOLD_L ) { //now looking for left sensor line
          moveForward();
        }
        stopMotors();
      }
      offset[1] = odometer.getXYT()[localizingAxis];
    }
    
    double turnTheta = Math.atan(Math.abs((offset[1] - offset[0]))/WHEEL_BASE);
    double turnThetaDeg = 180*turnTheta/Math.PI;
    
    if (shouldRight) {
      turnRight(turnThetaDeg);
    } else {
      turnLeft(turnThetaDeg);
    }
    
  }
  
}