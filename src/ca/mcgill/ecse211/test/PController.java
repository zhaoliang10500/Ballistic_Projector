package ca.mcgill.ecse211.test;
import static ca.mcgill.ecse211.test.Resources.*;

/**
 * This class implements the Wall Follower for Lab1 on the EV3 Platform for the Type-P
 * controller
 * 
 * @author Freiberger & Zhao
 *
 */
public class PController extends UltrasonicController {

  private static final int MOTOR_SPEED = 185;
  //Different gain in left and right motors
  private static final float PROPORTIONAL_LEFT = 0.65f;
  private static final float PROPORTIONAL_RIGHT = 1;
  //offset
  private final int DIFF_FACTOR = 8;
  private static final int MIN_SPEED = 95;
  private int diff, leftSpeed, rightSpeed;
 
  /**
   * Initialize motor to start the robot rolling forward
   */
  public PController() {
    LEFT_MOTOR.setSpeed(MOTOR_SPEED); 
    RIGHT_MOTOR.setSpeed(MOTOR_SPEED);
    LEFT_MOTOR.forward();
    RIGHT_MOTOR.forward();
  }

  /**
   * This is the method that constantly updates the values of distance detected by the US
   * sensor, as well as provides the appropriate reactions to each returned distance value.
   */
  @Override
  public void processUSData(int distance) {
    filter(distance);
    float straightDistance = (float) (distance / 1.4);
    this.distance = (int) straightDistance;
    
    int error = BAND_CENTER - this.distance;
    
    //If the vehicle is in the correct path, continue straight
    if (this.distance > BAND_CENTER - BAND_WIDTH 
        && this.distance < BAND_CENTER + BAND_WIDTH) {
      straight();
    }
    
    //If Vehicle is TOO FAR AWAY, turning left
    //Or a U-turn/ left turn.
    else if (this.distance > 40) { 
      LEFT_MOTOR.setSpeed(FWDSPEED - 60);
      RIGHT_MOTOR.setSpeed(FWDSPEED);
      LEFT_MOTOR.forward();
      RIGHT_MOTOR.forward();
    }

    //Turing left if a usual far away
    //slower turn is guaranteed by a smaller gain coefficient and a negative offset
    //to prevent sudden acceleration
    else if (this.distance > BAND_CENTER + BAND_WIDTH) {
      turnLeft(error);
    }
    
    //If Vehicle is TOO CLOSE to the wall
    else if (this.distance < 18) {
      LEFT_MOTOR.setSpeed(-FWDSPEED + 20);
      RIGHT_MOTOR.setSpeed(-FWDSPEED);
      LEFT_MOTOR.forward();
      RIGHT_MOTOR.backward();
    }
    
    //The vehicle is CLOSE to the wall turn right
    //concave corner
    else if (this.distance < BAND_CENTER - BAND_WIDTH) {
      turnRight(error);
    }
    
  }
  
  /**
   * Decides how much motor adjustment to make when the robot is too far from the wall
   * 
   * @param error: the distance that the robot is from the band center
   */
  private void turnLeft(int error) {
    diff = Math.max(MIN_SPEED, 
        (int)(Math.abs(error) * PROPORTIONAL_LEFT)) - DIFF_FACTOR;
    leftSpeed = MOTOR_SPEED - diff;
    rightSpeed = MOTOR_SPEED + diff;
    LEFT_MOTOR.setSpeed(leftSpeed);
    RIGHT_MOTOR.setSpeed(rightSpeed);
    LEFT_MOTOR.forward();
    RIGHT_MOTOR.forward();
  }
  
  /**
   * Decides how much motor adjustment to make when the robot is close to the wall
   * 
   * @param error the distance that the robot is from the band center
   */
  private void turnRight(int error) {
    diff = Math.max(MIN_SPEED, 
        (int)(Math.abs(error) * PROPORTIONAL_RIGHT));
    leftSpeed = MOTOR_SPEED + diff;
    rightSpeed = MOTOR_SPEED - diff;
    LEFT_MOTOR.setSpeed(leftSpeed);
    RIGHT_MOTOR.setSpeed(rightSpeed);
    LEFT_MOTOR.forward();
    RIGHT_MOTOR.forward();
  }
  
  /**
   * If the robot isn't too close or too far from the wall, continue straight
   */
  private void straight() {
    LEFT_MOTOR.setSpeed(MOTOR_SPEED);
    RIGHT_MOTOR.setSpeed(MOTOR_SPEED);
    LEFT_MOTOR.forward();
    RIGHT_MOTOR.forward();
  }

  /**
   * Reads the distance from the US sensor
   */
  @Override
  public int readUSDistance() {
    return this.distance;
  }

}
