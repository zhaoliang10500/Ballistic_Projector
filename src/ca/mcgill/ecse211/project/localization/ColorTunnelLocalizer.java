package ca.mcgill.ecse211.project.localization;
import static ca.mcgill.ecse211.project.game.Resources.*;
import ca.mcgill.ecse211.project.sensor.ColorUser;
import static ca.mcgill.ecse211.project.game.Helper.*;

public class ColorTunnelLocalizer implements ColorUser {
  private boolean localizing = false;
  private double[] initialColor = new double[2];
  private volatile boolean gotInitialSample = false;
  private volatile int step;
  private volatile double[]  offset = new double[2];
  private volatile boolean turnRight;
  public double backedupDist;
      
  public void localize() {    
    sleepFor(1000);
    
    //get yDistFromLine and adjust heading after localization
    localizing = true;
    step = 0;
    while(localizing);
    
    double yTheta = Math.atan((offset[1] - offset[0])/WHEEL_BASE);
    double yThetaDeg = 180*yTheta/Math.PI;
    
    if (turnRight) {
      turnRight(yThetaDeg);
    }
    else {
      turnLeft(yThetaDeg);
    }
    
    //backedupDist = odometer.getXYT()[1]; //used to make the localization work even if backed up one extra tile
        
    double yDistFromLine = odometer.getXYT()[1];
    moveBackward(yDistFromLine);
  }
  
//  public double backupDist() {
//    return backedupDist;
//  }
  
  @Override
  public void processColorData(double[] color) {
    if (!localizing) {
      return;
    }
    else if (!gotInitialSample) {
      moveForward(6.5);
      moveBackward(2);
      initialColor[0] = color[0];
      initialColor[1] = color[1];
      moveBackward();
      sleepFor(50);
      gotInitialSample = true; 
    }
  //for y, left sensor sees line and right doesn't
    else if (color[0]/initialColor[0] < COLOR_THRESHOLD_L && color[1]/initialColor[1] > COLOR_THRESHOLD_R) {
      switch(step) {
        case 0:
          stopMotors();
          turnRight = false;
          offset[0] = odometer.getXYT()[1];
          moveBackward();
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
    else if (color[0]/initialColor[0] > COLOR_THRESHOLD_L && color[1]/initialColor[1] < COLOR_THRESHOLD_R) {
      switch (step) {
        case 0:
          stopMotors();
          turnRight = true;
          offset[0] = odometer.getXYT()[1];
          moveBackward();
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
    else if (color[0]/initialColor[0] < COLOR_THRESHOLD_L && color[1]/initialColor[1] < COLOR_THRESHOLD_R) {
      stopMotors();
      offset[0] = 0;
      offset[1] = 0;
      localizing = false;
    }
  }
}