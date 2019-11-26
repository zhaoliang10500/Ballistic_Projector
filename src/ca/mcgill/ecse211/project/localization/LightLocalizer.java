package ca.mcgill.ecse211.project.localization;
import static ca.mcgill.ecse211.project.game.Resources.*;
import ca.mcgill.ecse211.project.sensor.LightUser;
import static ca.mcgill.ecse211.project.game.Helper.*;
import ca.mcgill.ecse211.project.game.WiFi;


/**
 * This class contains methods for light sensor localization
 * The robot uses two light sensors placed at each side at the back of its body 
 * to read grid lines and correct its heading (angle) as necesary
 */
public class LightLocalizer implements LightUser {
  private boolean localizing = false;
  private double[] initialLight = new double[2];
  private int firstAxis = -1;
  private int secondAxis = -1;
  private volatile boolean gotInitialSample = false;
  private volatile int step;
  private volatile double[]  offset = new double[2];
  private volatile boolean turnRight;
  private volatile boolean findingFirst;
  private final double angleOffset = 0; //7 or y, turn less than 90 to better localize
  
  /**
   * Method to begin light localization
   */
  public void localize() {
    //get firstDistFromLine roughly
    localizing = true;
    findingFirst = true;
    if (WiFi.CORNER == 0 || WiFi.CORNER == 2) {
      firstAxis = 0; //first displacement is x
      secondAxis = 1;
    }
    else if (WiFi.CORNER == 1 || WiFi.CORNER == 3) {
      firstAxis = 1; //first displacement is y
      secondAxis = 0;
    }
    while (localizing);
    
    //get secondDistFromLine and adjust heading after localization
    setLRMotorSpeed(LS_SPEED_FAST);
    turnLeft(90 - angleOffset);
    localizing = true;
    step = 0;
    setLRMotorSpeed(LS_SPEED_SLOW);
    moveForward();
    while(localizing);
  }
  
  public double turnThetaDeg() {
    double turnTheta = Math.atan(Math.abs((offset[1] - offset[0]))/WHEEL_BASE);
    double turnThetaDeg = 180*turnTheta/Math.PI;
    return turnThetaDeg;
  }
  
  public boolean turnRight() {
    return turnRight;
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
      setLRMotorSpeed(LS_SPEED_FAST);
      moveForward(7);
      moveBackward(2);
      initialLight[0] = light[0];
      initialLight[1] = light[1];
      if (firstAxis == 0) {
        odometer.setX(0);
      }
      else if (firstAxis == 1) {
        odometer.setY(0);
      }
      sleepFor(100);
      setLRMotorSpeed(LS_SPEED_SLOW);
      moveForward();
      sleepFor(100);
      gotInitialSample = true;
    }
    //find firstDistFromLine roughly, no adjustments (to save time)
    else if (findingFirst == true && (light[0]/initialLight[0] < LIGHT_THRESHOLD_L || light[1]/initialLight[1] < LIGHT_THRESHOLD_R)) {
      stopMotors();
      setLRMotorSpeed(LS_SPEED_FAST);
      moveBackward(LS_DISTANCE + WHEEL_BASE*Math.sin(Math.PI*angleOffset/180)); //backup more to compensate for angleOffset
      sleepFor(100);
      findingFirst = false;
      localizing = false;
    }
    //left sensor sees line and right doesn't
    else if (light[0]/initialLight[0] < LIGHT_THRESHOLD_L && light[1]/initialLight[1] > LIGHT_THRESHOLD_R ) {
      switch(step) {
        case 0:
          stopMotors();
          turnRight = false;
          offset[0] = odometer.getXYT()[secondAxis];
          setLRMotorSpeed(LS_SPEED_SLOW);
          moveForward();
          sleepFor(LS_SPEED_FAST);
          step ++;
          break;
        case 1:
          stopMotors();
          offset[1] = odometer.getXYT()[secondAxis];
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
          turnRight = true;
          offset[0] = odometer.getXYT()[secondAxis];
          setLRMotorSpeed(LS_SPEED_SLOW);
          moveForward();
          sleepFor(LS_SPEED_FAST);
          step++;
          break;
        case 1:
          stopMotors();
          offset[1] = odometer.getXYT()[secondAxis];
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
