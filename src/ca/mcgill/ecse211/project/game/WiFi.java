package ca.mcgill.ecse211.project.game;

import static ca.mcgill.ecse211.project.game.WifiResources.*;

/**
 * This class takes input from the server at the beginning of the program
 *
 */
public class WiFi {
  public static volatile boolean recievedParameters = false;
  
  /**
   * Method to get team color and corresponding boundary parameters from server
   */
  public static void wifi() {
    System.out.println("Map:\n" + wifiParameters);
    recievedParameters = true;
  }
}