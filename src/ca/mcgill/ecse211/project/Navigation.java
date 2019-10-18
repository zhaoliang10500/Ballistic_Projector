package ca.mcgill.ecse211.project;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import static ca.mcgill.ecse211.project.Resources.*;
/**
 * @author zhaoliang & Brandon
 * This Class implemented the navigation function to gridpoint(1,1)
 */
public class Navigation {
  
  private static EV3LargeRegulatedMotor leftMotor;
  private static EV3LargeRegulatedMotor rightMotor;
  private static Odometer odometer;
  private LightLocalizer lightLocalizer;
  
  public Navigation (EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
      Odometer odometer, LightLocalizer lightLocalizer) {
    Navigation.leftMotor = leftMotor;
    Navigation.rightMotor = rightMotor;
    Navigation.odometer = odometer;
    this.lightLocalizer = lightLocalizer;
  }
  
  public void doNavigation () {
    //first move to the black line of x-axis side.
    while (lightLocalizer.isDetectedBlackLineOfXSide != 1) {
      leftMotor.forward();
      rightMotor.forward();
    }
    //detected a black line, backwards for a bit
    leftMotor.rotate(-180, true);
    rightMotor.rotate(-180, false);
    
    //change to 270 degrees which means it is now facing the black line of y-axis side.
    leftMotor.rotate(240, true);
    rightMotor.rotate(-240, false);
    while (lightLocalizer.isDetectedBlackLineOfYSide != 1) {
      leftMotor.forward();
      rightMotor.forward();
    }
    leftMotor.rotate(-180, true);
    rightMotor.rotate(-180, false);
    
    //now we can go to (1,1) gridpoint, we know we are 180units from x and y axis
    //turn to the 45 degree line
    leftMotor.rotate(-120, true);
    rightMotor.rotate(120, false);
    
    //255^2 = 180^2 + 180^2, 55 is the adjustment of errors.
    leftMotor.rotate(255 + 190, true);
    rightMotor.rotate(255 + 190, false);
    
    leftMotor.rotate(-120, true);
    rightMotor.rotate(120, false);
  }
  
  /**
   * @param double angle representing the angle heading change in radians 
   * @return minimum degrees needed to turn to the required angle
   */
  public static double getMinAngle(double angle) {
    if (angle > Math.PI) {
      angle = 2 * Math.PI - angle;
    }
    else if (angle < -Math.PI) {
      angle = angle + 2 * Math.PI;
    }
    return angle;
  }
  
  /** parameters: double theta that represents an angle in radians
   *  action: changes direction from current angle to theta
   */
  public static void turnTo(double theta) {
      double angle = theta- odometer.getXYT()[2];
      
      leftMotor.rotate(changeToDesiredAngle(getMinAngle(angle)),true);
      rightMotor.rotate(-changeToDesiredAngle(getMinAngle(angle)),false);
  }

  /** parameter: double angle representing the angle heading change in radians
   *  returns: degrees the motors have to turn to change this direction
   */
  private static int changeToDesiredAngle(double angle){
      return travelToNextWaypoint(WHEEL_BASE*angle/2);
  }
  
//****Normal Travel mode used in Simple navigation******
  public static void travelTo(double x, double y) {
      
    //reset motors
    leftMotor.stop();
    rightMotor.stop();
    leftMotor.setAcceleration(600);
    rightMotor.setAcceleration(600);
    
    //calculate trajectory path and angle
    double trajectoryX = x;
    double trajectoryY = y;
    double trajectoryAngle = Math.atan2(trajectoryX, trajectoryY);
    
    //rotate to correct angle
    Sound.beepSequenceUp();
    leftMotor.setSpeed(ROTATION_SPEED);
    rightMotor.setSpeed(ROTATION_SPEED);
    turnTo(trajectoryAngle);
    
    double trajectoryLine = Math.hypot(trajectoryX, trajectoryY);
    
    //move forward correct distance
    Sound.beepSequence();
    leftMotor.setSpeed(FORWARD_SPEED);
    rightMotor.setSpeed(FORWARD_SPEED);
    leftMotor.rotate(travelToNextWaypoint(trajectoryLine),true);
    rightMotor.rotate(travelToNextWaypoint(trajectoryLine),false);
  }
  
  /** parameter: double distance indicates how far the car should run
   *  returns: degress the wheels have to turn to get to the next waypoint
   */
  private static int travelToNextWaypoint(double distance){
      return (int) (180*distance/(Math.PI * WHEEL_RADIUS));
  }
}
