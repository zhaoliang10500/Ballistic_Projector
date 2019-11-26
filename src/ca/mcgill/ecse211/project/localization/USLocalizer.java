package ca.mcgill.ecse211.project.localization;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import static ca.mcgill.ecse211.project.game.Resources.*;
import java.util.Arrays;
import ca.mcgill.ecse211.project.odometry.Odometer;
import static ca.mcgill.ecse211.project.game.Helper.*;
/**
 * This Class implemented the ultrasonic sensor localization
 *  @author Liang Zhao & Jessie Tang
 */
public class USLocalizer {
  private int filterSize = 3;
  private int[] tempDists = new int[filterSize];
  
  //Set up here
  private Odometer odometer;
  private SampleProvider usSensor;
  private float[] usData;
  private EV3LargeRegulatedMotor leftMotor, rightMotor;
  
  public USLocalizer (EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, 
      Odometer odometer, SampleProvider usSensor, float[] usData) {
    this.odometer = odometer;
    this.usSensor = usSensor;
    this.usData = usData;
    this.leftMotor = leftMotor;
    this.rightMotor = rightMotor;
    leftMotor.setAcceleration(ACCELERATION);
    rightMotor.setAcceleration(ACCELERATION);
  }
  
  
  /*
   * implement the method to use ultrasonic sensor to detect the 0 degree direction
   * and make the car face to this degree
   */
  public void doLocalization() {
    
    double angle;
    leftMotor.setSpeed(ROTATION_SPEED);
    rightMotor.setSpeed(ROTATION_SPEED);
    
    /*
     * if "FALLING_EDGE" is chose, first rotate the car to the right to detect the falling edge
     * the FALL_EDGE is based on the right edge, theta is 0 when detected the right edge
     * then rotate back to detect the falling edge on the other side.
     * Recommend to use when the car is facing away from the wall
     */
    //Detect if the vehicle is facing away or forwards the wall
    //if it is facing away from the wall, turn right unless it detects the wall
    if (meanFilter() > DISTANCE_TO_WALL) {
      while (meanFilter() > DISTANCE_TO_WALL) {
        leftMotor.forward();
        rightMotor.backward();
      }
      leftMotor.stop(true);
      rightMotor.stop(false);
    }
    //if it is facing forwards the wall, turn right until it no more sees the wall
    else if (meanFilter() < DISTANCE_TO_WALL) {
      while (meanFilter() < DISTANCE_TO_WALL) {
        leftMotor.forward();
        rightMotor.backward();
      }
      //rotate right a bit to make the car keep rotating
      leftMotor.rotate(180, true);
      rightMotor.rotate(-180, false);
      //Rotate the car until it sees the edge on the right, and set this angle to 0
      while (meanFilter() > DISTANCE_TO_WALL) {
        leftMotor.forward();
        rightMotor.backward();
      }
      leftMotor.stop(true);
      rightMotor.stop(false);
    }
    odometer.setTheta(0.0);
    
    //turning left a bit to avoid double detection
    leftMotor.rotate(-220, true);
    rightMotor.rotate(220, false);
    //Rotate the car until it sees a wall, and then Store this angle
    while (meanFilter() > DISTANCE_TO_WALL) {
      leftMotor.backward();
      rightMotor.forward();
    }
    //stop the motors on the falling edge on the left.
    rightMotor.stop(true);
    leftMotor.stop(false);
    
    angle = odometer.getXYT()[2];
    
    angle = 360 - angle;
    
    //half of the angle + 45 degrees to get to the 0 degree direction
    double headingToZero = angle / 2 + 47;
    
    leftMotor.rotate(convertAngle(WHEEL_RADIUS, WHEEL_BASE, headingToZero), true);
    rightMotor.rotate(-convertAngle(WHEEL_RADIUS, WHEEL_BASE, headingToZero), false);
    
    odometer.setTheta(0.0);
    
    turnLeft(90);
    moveForward(4.1);
    turnRight(90);
  
  }
  
  //Conversion methods.
  private static int convertDistance(double radius, double distance) {
      return (int) ((180.0 * distance) / (Math.PI * radius));
  }
  
  private static int convertAngle(double radius, double width, double angle) {
    return convertDistance(radius, Math.PI * width * angle / 360.0);
}
  
  
  private int meanFilter() {
    int distance;
    for (int i = 0; i < filterSize; i++) { 
      usSensor.fetchSample(usData, 0); 
      tempDists[i] = (int) (usData[0] * 100.0); 
    }
    
    Arrays.sort(tempDists);
    distance = tempDists[filterSize/2]; //java rounds down for int division
    return distance;
  }
}
