package ca.mcgill.ecse211.project.localization;
import static ca.mcgill.ecse211.project.game.Resources.*;
import ca.mcgill.ecse211.project.sensor.ColorUser;
import static ca.mcgill.ecse211.project.game.Helper.*;


/**
 * This class contains methods for color sensor localization
 */
public class ColorLocalizer implements ColorUser {
  private boolean localizing = false;
  private double[] initialColor = new double[2];
  private volatile boolean gotInitialSample = false;
  private volatile int step;
  private volatile double[]  offset = new double[2];
  private volatile boolean turnRight;
  private volatile boolean findingX;
  private final double angleOffset = 5; //for y, turn 5 degrees less than 90 to better localize

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
    
    odometer.setXYT(TILE_SIZE, TILE_SIZE, 0);
  }

  @Override
  public void processColorData(double[] color) {
    if (!localizing) {
      return;
    }
    else if (!gotInitialSample) {
      moveForward(4);
      moveBackward(2);
      initialColor[0] = color[0];
      initialColor[1] = color[1];
      moveForward();
      sleepFor(100);
      gotInitialSample = true; 
    }
    //find xDistFromLine roughly, no adjustments (to save time)
    else if (findingX == true && (color[0]/initialColor[0] < COLOR_THRESHOLD || color[1]/initialColor[1] < COLOR_THRESHOLD)) {
      stopMotors();
      double xDistFromLine = odometer.getXYT()[0];
      moveBackward(xDistFromLine + WHEEL_BASE*Math.sin(Math.PI*angleOffset/180)); //backup x more to compensate for angleOffset
      findingX = false;
      localizing = false;
    }
    //for y, left sensor sees line and right doesn't
    else if (color[0]/initialColor[0] < COLOR_THRESHOLD && color[1]/initialColor[1] > COLOR_THRESHOLD) {
      switch(step) {
        case 0:
          stopMotors();
          turnRight = false;
          offset[0] = odometer.getXYT()[1];
          moveForward();
          sleepFor(100);
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
    else if (color[0]/initialColor[0] > COLOR_THRESHOLD && color[1]/initialColor[1] < COLOR_THRESHOLD) {
      switch (step) {
        case 0:
          stopMotors();
          turnRight = true;
          offset[0] = odometer.getXYT()[1];
          moveForward();
          sleepFor(100);
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
    else if (color[0]/initialColor[0] < COLOR_THRESHOLD && color[1]/initialColor[1] < COLOR_THRESHOLD) {
      stopMotors();
      offset[0] = 0;
      offset[1] = 0;
      localizing = false;
    }

  }

}
