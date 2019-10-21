package ca.mcgill.ecse211.project;

import static ca.mcgill.ecse211.project.Resources.*;

public class SimpleThrow {
  
  /**
   * Turns launch motors to throw a ball 4.5 tiles
   */
  public void doSimpleThrow () {

    leftThrowMotor.setAcceleration(50000);
    rightThrowMotor.setAcceleration(50000);
    leftThrowMotor.rotate(-70, true);
    rightThrowMotor.rotate(-70, false);
    leftThrowMotor.setAcceleration(2000);
    rightThrowMotor.setAcceleration(2000);
    leftThrowMotor.rotate(120, true);
    rightThrowMotor.rotate(120, false);
  }
}