package ca.mcgill.ecse211.project.localization;
import static ca.mcgill.ecse211.project.game.Resources.*;
import ca.mcgill.ecse211.project.sensor.ColorUser;
import static ca.mcgill.ecse211.project.game.Helper.*;


/**
 * This class contains methods for color sensor localization
 */
public class ColorLocalizer implements ColorUser {
  public int isDetectedBlackLineOfXSide = 0;
  public int isDetectedBlackLineOfYSide = 0;

  private boolean localizing = false;
  private double[] initialColor = new double[2];
  private volatile boolean gotInitialSample = false;
  private volatile int step;
  private volatile double[]  offset = new double[2];
  private volatile boolean turnRight;
  private volatile boolean localizingX = true;

  public void localize() {
    //wait for other thread (temp)
    sleepFor(2000);
    
    turnRight(90);
    localizing = true;
    step = 0;
    while (localizing);
    double xTheta = Math.atan((offset[1] - offset[0])/WHEEL_BASE);
    double xThetaDeg = 180*xTheta/Math.PI;  
    boolean finishedX = false;
    if (turnRight) {
      turnRight(xThetaDeg);
    }
    else {
      turnLeft(xThetaDeg);
    }
    
    double xDistFromLine = odometer.getXYT()[0];
    moveBackward(xDistFromLine);
    finishedX = true;
    localizingX = false;
    
    if (finishedX) {
      turnLeft(90);
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
    }
    
    System.out.println("offset: " + offset[0] + ", " + offset[1]);
  }

  @Override
  public void processColorData(double[] color) {
    if (!localizing) {
      return;
    }
    else if (!gotInitialSample) {
      moveForward(5);
      moveBackward(2);
      initialColor[0] = color[0];
      initialColor[1] = color[1];
      moveForward();
      gotInitialSample = true; 
    }
    //left sensor sees line and right doesn't
    else if (color[0]/initialColor[0] < COLOR_THRESHOLD && color[1]/initialColor[1] > COLOR_THRESHOLD) {
      switch(step) {
        case 0:
          stopMotors();
          if (localizingX) {
            offset[0] = odometer.getXYT()[0];
          } 
          else {
            offset[0] = odometer.getXYT()[1];
          }
          moveForward();
          turnRight = false;
          step ++;
          break;
        case 1:
          stopMotors();
          if (localizingX) {
            offset[1] = odometer.getXYT()[0];
          } 
          else {
            offset[1] = odometer.getXYT()[1];
          }
          step++;
          localizing = false;
          break;  
      }
    }
    //right sensor sees line and left doesn't
    else if (color[0]/initialColor[0] > COLOR_THRESHOLD && color[1]/initialColor[1] < COLOR_THRESHOLD) {
      switch (step) {
        case 0:
          stopMotors();
          if (localizingX) {
            offset[0] = odometer.getXYT()[0];
          } 
          else {
            offset[0] = odometer.getXYT()[1];
          }
          moveForward();
          turnRight = true;
          step++;
          break;
        case 1:
          stopMotors();
          if (localizingX) {
            offset[1] = odometer.getXYT()[0];
          } 
          else {
            offset[1] = odometer.getXYT()[1];
          }
          step++;
          localizing = false;
          break;  
      }
    }
    //both sensors see line at the same time
    else if (color[0]/initialColor[0] < COLOR_THRESHOLD && color[1]/initialColor[1] < COLOR_THRESHOLD) {
      stopMotors();
      offset[0] = 0;
      offset[1] = 0;
      localizing = false;
    }

  }

}
