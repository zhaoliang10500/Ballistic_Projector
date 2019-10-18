package ca.mcgill.ecse211.project;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import static ca.mcgill.ecse211.project.Resources.*;

public class SimpleThrow {
  
  //motors
  private EV3LargeRegulatedMotor leftThrowMotor, rightThrowMotor;
  Navigation navi; //the navigation class
  
  public SimpleThrow(EV3LargeRegulatedMotor leftThrowMotor, EV3LargeRegulatedMotor rightThrowMotor) {
      //set up motors
      this.leftThrowMotor = leftThrowMotor;
      this.rightThrowMotor = rightThrowMotor;
      this.leftThrowMotor.setAcceleration(LAUNCHING_ACCELERATION + 50000);
      this.rightThrowMotor.setAcceleration(LAUNCHING_ACCELERATION + 50000);
  }
  
  public void doSimpleThrow () {
    leftThrowMotor.rotate(-70, true);
    rightThrowMotor.rotate(-70, false);
    leftThrowMotor.rotate(60, true);
    rightThrowMotor.rotate(60, false);
  }
}