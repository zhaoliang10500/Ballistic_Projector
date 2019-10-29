package ca.mcgill.ecse211.test;

import lejos.hardware.Button;
import static ca.mcgill.ecse211.test.Resources.*;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import ca.mcgill.ecse211.test.UltrasonicPoller;

/**
 * Main class of the program.
 */
public class Main {
  /**
   * Main entry point - instantiate objects used and set up sensor
   * @param args
   * @throws FileNotFoundException 
   */
  public static void main(String[] args) throws FileNotFoundException {
    PrintWriter writer = new PrintWriter("data.txt");
    LEFT_MOTOR.setAcceleration(50);
    // Set up the display on the EV3 screen and wait for a button press. 
    // The button ID (option) determines what type of control to use
    int option = Button.waitForAnyPress();
    LEFT_MOTOR.resetTachoCount();
    LCD.drawString("Press left or right button to start", 0, 4); //change here to switch motor
    if (option == Button.ID_LEFT || option == Button.ID_RIGHT) {
      new Thread(new UltrasonicPoller(writer)).start();
      for (int i = 0; i < 5; i++) {
        LEFT_MOTOR.rotate(60);
        LEFT_MOTOR.rotate(-120);
        LEFT_MOTOR.rotate(60);
      }
      writer.close();
    } else {
      showErrorAndExit("Error - invalid button!");
    }
    System.exit(0);
  }

  /**
   * Shows error and exits program.
   */
  public static void showErrorAndExit(String errorMessage) {
    LCD.clear();
    System.err.println(errorMessage);
    
    // Sleep for 2 seconds so user can read error message
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
    }
    
    System.exit(-1);
  }
  
}
