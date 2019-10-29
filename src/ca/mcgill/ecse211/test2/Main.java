// Lab2.java  
package ca.mcgill.ecse211.test2;

import lejos.hardware.Button;
import static ca.mcgill.ecse211.project.Resources.LCD;
// static import to avoid duplicating variables and make the code easier to read
import static ca.mcgill.ecse211.test2.Resources.*;

/**
 * The main driver class for the odometry lab.
 */
public class Main {

  /**
   * The main entry point.
   * 
   * @param args
   */
  public static void main(String[] args) {
    int buttonChoice;
    new Thread(odometer).start(); // Odometer
    
//    buttonChoice = chooseDriveInSquareOrFloatMotors();
//
//    if (buttonChoice == Button.ID_LEFT) {
//      floatMotors();
//    } else {
//      buttonChoice = chooseCorrectionOrNot();
//      if (buttonChoice == Button.ID_RIGHT) {
//        new Thread(new OdometryCorrection()).start(); // OdometryCorrection
//      }
//      SquareDriver.drive();
//    }
    
    do {
      LCD.clear();
      LCD.drawString("< Left  | Right >", 0, 0);
      LCD.drawString("        |        ", 0, 1);
      LCD.drawString(" Test   | Test   ", 0, 2);
      LCD.drawString("Odometer| ColorS ", 0, 3);

      buttonChoice = Button.waitForAnyPress();

    } while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT && buttonChoice != Button.ID_ESCAPE);
    
    if (buttonChoice == Button.ID_LEFT) {
      int buttonpressed;
      do {
        buttonpressed = Button.waitForAnyPress();
        SquareDriver.drive();
      } while (buttonpressed != Button.ID_ESCAPE); 
      if (buttonpressed == Button.ID_ESCAPE) {
        System.exit(0);
      }
    } 
    else if (buttonChoice == Button.ID_RIGHT) {
      new Thread(new ColorTest()).start();
      int buttonpressed;
      do {
        buttonpressed = Button.waitForAnyPress();
        SquareDriver.drive();
      } while (buttonpressed != Button.ID_ESCAPE); 
      if (buttonpressed == Button.ID_ESCAPE) {
        System.exit(0);
      }
    }
    
    new Thread(new Display()).start();
    while (Button.waitForAnyPress() != Button.ID_ESCAPE) {
    } // do nothing
    
    System.exit(0);
  }

  /**
   * Sleeps current thread for the specified duration.
   * 
   * @param duration sleep duration in milliseconds
   */
  public static void sleepFor(long duration) {
    try {
      Thread.sleep(duration);
    } catch (InterruptedException e) {
      // There is nothing to be done here
    }
  }
  
}
