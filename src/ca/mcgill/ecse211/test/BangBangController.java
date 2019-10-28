package ca.mcgill.ecse211.test;

import static ca.mcgill.ecse211.test.Resources.*;

/**
 * This class implements the Wall Follower for Lab1 on the EV3 Platform for the Bang Bang
 * controller
 * 
 * @author Freiberger & Zhao
 *
 */
public class BangBangController extends UltrasonicController {

  //Define constants and variables
  private float straightDistance = 0;
 
  /**
   * Initialize motor to start the robot rolling forward
   */
  public BangBangController() {
    LEFT_MOTOR.setSpeed(MOTOR_HIGH); 
    RIGHT_MOTOR.setSpeed(MOTOR_HIGH);
    LEFT_MOTOR.forward();
    RIGHT_MOTOR.forward();
  }

  /**
   * This is the method that constantly updates the values of distance detected by the US
   * sensor, as well as provides the appropriate reactions to each returned distance value.
   * 
   * Unlike the P controller, all actions and computations are calculated in this method.
   * The if statements refer to the different cases of the different distances of the robot
   * from the wall
   */
  @Override
  public void processUSData(int distance) {
    filter(distance);
    
    straightDistance = (float) (distance / 1.4);
    this.distance = (int) straightDistance;
    
    //If the distance is correct
    if (this.distance > BAND_CENTER - BAND_WIDTH 
        && this.distance < BAND_CENTER + BAND_WIDTH) {
      LEFT_MOTOR.setSpeed(FWDSPEED);
      RIGHT_MOTOR.setSpeed(FWDSPEED);
      LEFT_MOTOR.forward();
      RIGHT_MOTOR.forward();
    }
    //If Vehicle is TOO FAR AWAY, turning left
    // Or a U-turn/ left turn.
    else if (this.distance > 40) { 
      LEFT_MOTOR.setSpeed(FWDSPEED - 60);
      RIGHT_MOTOR.setSpeed(FWDSPEED);
      LEFT_MOTOR.forward();
      RIGHT_MOTOR.forward();
    }
    //Turing left if a usual far away
    else if (this.distance > BAND_CENTER + BAND_WIDTH) {
      LEFT_MOTOR.setSpeed(FWDSPEED - 105);
      RIGHT_MOTOR.setSpeed(FWDSPEED);
      LEFT_MOTOR.forward();
      RIGHT_MOTOR.forward();
    }
    //If Vehicle is TOO CLOSE to the wall
    else if (this.distance < 15) {
      LEFT_MOTOR.setSpeed(-FWDSPEED + 20);
      RIGHT_MOTOR.setSpeed(-FWDSPEED);
      LEFT_MOTOR.forward();
      RIGHT_MOTOR.backward();
    }
    //If Vehicle is CLOSE to the wall
    else if (this.distance < BAND_CENTER - BAND_WIDTH) {
      LEFT_MOTOR.setSpeed(FWDSPEED);
      RIGHT_MOTOR.setSpeed(FWDSPEED - 121);
      LEFT_MOTOR.forward();
      RIGHT_MOTOR.forward();
    }
  }
  
  /**
   * Reads the sensor to see how far the robot is from the wall
   */
  @Override
  public int readUSDistance() {
    return this.distance;
  }
}
