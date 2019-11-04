package ca.mcgill.ecse211.project.localization;
import lejos.hardware.Sound;
import static ca.mcgill.ecse211.project.game.Helper.*;
import static ca.mcgill.ecse211.project.game.Resources.*;
import ca.mcgill.ecse211.project.sensor.ColorUser;


/**
 * This class contains methods for color sensor localization
 */
public class ColorLocalizer implements ColorUser {
  //public boolean localizing = false;
  //TODO: might need to change color localizer and use this boolean

  private int currentVal = -1;
  private int lineCounter = 0;
  private double[] angles = new double[4];
  private int colorThreshold;
  //TODO: find a way to take an initial sample so we can use differential instead of absolute value
  private int[] colors;


  /**
   * Runs the logic of the light localizer
   */
  public void localize() {
    // initial position adjustment to move closer to origin
    //TODO: change this depending on current hardware
    turnRight(90);
    moveBackward(4);  
    turnLeft(90);
    moveBackward(5);

    odometer.setTheta(0);

    // Localize
    // both motors non-blocking to allow gridline detection while turning
    leftMotor.rotate(convertAngle(360, WHEEL_RADIUS), true);
    rightMotor.rotate(-convertAngle(360, WHEEL_RADIUS), true);
    //TODO: add booleans maybe? test first

    //calculate x and y offsets from origin
    double thetaY = angles[2] - angles[0];
    double xError = 35; //the measured xTheta tend to be too large due to LS inaccuracies
    double thetaX = angles[0] + angles[1] + (360-angles[3]-xError);

    //TODO: re-measure and calculate distance according to left sensor
    double dy = CS_DISTANCE*Math.cos(thetaX/2*Math.PI/180);
    double dx = CS_DISTANCE*Math.cos(thetaY/2*Math.PI/180);

    odometer.setY(-dy + TILE_SIZE);
    odometer.setX(-dx + TILE_SIZE );

    // Move robot's center of rotation to (1,1) and turn to 0 degrees
    turnRight(90);
    moveForward(dx);
    turnLeft(90);
    moveForward(dy);
  }   

  /**
   * Compares current color with next color to determine if a line is crossed
   * @param nextVal
   * @return if a line is crossed or not
   */
  private boolean lineCrossed(int nextVal) {
    boolean lineCrossed = false;
    if (currentVal == -1) {
      currentVal = nextVal;
    }
    else {
      if (Math.abs(currentVal - nextVal) > COLOR_THRESHOLD) {
        lineCrossed = true;
      }
      currentVal = nextVal;
    }
    return lineCrossed;
  }

  /**
   * Method to process color poller data
   * Only uses left color sensor data = colors[0] to localize
   */
  @Override
  public void processColorData(int[] colors) {
    //TODO: might need this, test first
    //    if (!localizing) {
    //      return;
    //    }
    //    else {
    //      this.colors = colors;
    //    }
    boolean lineCrossed = lineCrossed(colors[0]);
    this.colors = colors;

    if (lineCrossed) {
      Sound.beep();
      switch(lineCounter) {
        case 0:
          angles[0] = odometer.getXYT()[2];
          break;
        case 1:
          angles[1] = odometer.getXYT()[2];
          break;
        case 2:
          angles[2] = odometer.getXYT()[2];
          break;
        case 3:
          angles[3] = odometer.getXYT()[2];
          break;
      }
      lineCounter++;
    }
    //TODO: might need to add another boolean, test first
    //    else if (!lineCrossed && onLine) {
    //      onLine = false;
    //      lineCounter++;
    //      lineCounter %= 4;
    //    }
  }

}