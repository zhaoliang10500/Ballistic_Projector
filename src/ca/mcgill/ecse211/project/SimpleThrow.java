package ca.mcgill.ecse211.project;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import static ca.mcgill.ecse211.project.Resources.*;

public class SimpleThrow {
  
  //motors
  private EV3LargeRegulatedMotor leftThrowMotor, rightThrowMotor;
  
  public SimpleThrow(EV3LargeRegulatedMotor leftThrowMotor, EV3LargeRegulatedMotor rightThrowMotor) {
      //set up motors
      this.leftThrowMotor = leftThrowMotor;
      this.rightThrowMotor = rightThrowMotor;
  }
  
  public void doSimpleThrow () {
    this.leftThrowMotor.setAcceleration(2000);
    this.rightThrowMotor.setAcceleration(2000);
    leftThrowMotor.rotate(-10, true);
    rightThrowMotor.rotate(-10, false);
    this.leftThrowMotor.setAcceleration(50000);
    this.rightThrowMotor.setAcceleration(50000);
    leftThrowMotor.rotate(-70, true);
    rightThrowMotor.rotate(-70, false);
    this.leftThrowMotor.setAcceleration(2000);
    this.rightThrowMotor.setAcceleration(2000);
    leftThrowMotor.rotate(120, true);
    rightThrowMotor.rotate(120, false);
  }
}