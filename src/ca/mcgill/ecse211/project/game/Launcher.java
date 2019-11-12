package ca.mcgill.ecse211.project.game;

import static ca.mcgill.ecse211.project.game.Resources.*;

/**
 * This class contains the ball launcher method
 *
 */
public class Launcher {
  
  /**
   * Turns launch motor to throw a ball 4.5 tiles
   */
  public static void launch() {

    throwMotor.setSpeed(90);
    throwMotor.rotate(-1000, true);
  }
}