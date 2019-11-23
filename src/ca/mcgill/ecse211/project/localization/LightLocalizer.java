package ca.mcgill.ecse211.project.localization;
import static ca.mcgill.ecse211.project.game.Resources.*;
import ca.mcgill.ecse211.project.sensor.LightUser;
import static ca.mcgill.ecse211.project.game.Helper.*;
import static ca.mcgill.ecse211.project.game.WiFi.*;
import ca.mcgill.ecse211.project.game.WiFi;


/**
 * This class contains methods for light sensor localization
 * The robot uses two light sensors placed at each side at the back of its body 
 * to read grid lines and correct its heading (angle) as necesary
 */
public class LightLocalizer implements LightUser {
  private boolean localizing = false;
  private double[] initialLight = new double[2];
  private volatile boolean gotInitialSample = false;
  private volatile int step;
  private volatile double[]  offset = new double[2];
  private volatile boolean turnRight;
  private volatile boolean findingX;
  private final double angleOffset = 5; //7 or y, turn less than 90 to better localize
  
  /**
   * Method to begin light localization
   */
  public void localize() {
    //get xDistFromLine roughly
    localizing = true;
    findingX = true;
    while (localizing);
    
    //get yDistFromLine and adjust heading after localization
    turnLeft(90 - angleOffset);
    localizing = true;
    step = 0;
    moveForward();
    while(localizing);
    double yTheta = Math.atan((offset[1] - offset[0])/WHEEL_BASE);
    double yThetaDeg = 180*yTheta/Math.PI;
    if (turnRight) {
      turnRight(yThetaDeg);
    }
    else {
      turnLeft(yThetaDeg);
    }
    double yDistFromLine = odometer.getXYT()[1];
    moveBackward(yDistFromLine);
    
    if (WiFi.CORNER == 0) {
      odometer.setXYT(TILE_SIZE, TILE_SIZE, 0);
    }
    else if (WiFi.CORNER == 1) {
      odometer.setXYT(14*TILE_SIZE, TILE_SIZE, 270);
    }
    else if (WiFi.CORNER == 2) {
      odometer.setXYT(14*TILE_SIZE, 8*TILE_SIZE, 180);
    }
    else if (WiFi.CORNER == 3) {
      odometer.setXYT(TILE_SIZE, 8*TILE_SIZE, 90);
    }
  }
  
  /**
   * Method to process light poller data
   * Implemented from interface LightUser
   */
  @Override
  public void processLightData(int[] light) {
    if (!localizing) {
      return;
    }
    else if (!gotInitialSample) {
      //moveForward(3);
      //moveBackward(2);
      moveForward(5);
      moveBackward(3);
      
      initialLight[0] = light[0];
      initialLight[1] = light[1];
      moveForward();
      sleepFor(100);
      gotInitialSample = true; 
    }
    //find xDistFromLine roughly, no adjustments (to save time)
    else if (findingX == true && (light[0]/initialLight[0] < LIGHT_THRESHOLD_L || light[1]/initialLight[1] < LIGHT_THRESHOLD_R)) {
      stopMotors();
      double xDistFromLine = odometer.getXYT()[0];
      moveBackward(xDistFromLine + WHEEL_BASE*Math.sin(Math.PI*angleOffset/180)); //backup x more to compensate for angleOffset
      findingX = false;
      localizing = false;
    }
    //for y, left sensor sees line and right doesn't
    else if (light[0]/initialLight[0] < LIGHT_THRESHOLD_L && light[1]/initialLight[1] > LIGHT_THRESHOLD_R) {
      switch(step) {
        case 0:
          stopMotors();
          turnRight = false;
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
    //for x, right sensor sees line and left doesn't
    else if (light[0]/initialLight[0] > LIGHT_THRESHOLD_L && light[1]/initialLight[1] < LIGHT_THRESHOLD_R) {
      switch (step) {
        case 0:
          stopMotors();
          turnRight = true;
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
