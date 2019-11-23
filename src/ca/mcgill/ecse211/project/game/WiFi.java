package ca.mcgill.ecse211.project.game;

import static ca.mcgill.ecse211.project.game.WifiResources.*;
import static ca.mcgill.ecse211.project.game.Helper.sleepFor;

/**
 * This class takes input from the server at the beginning of the program
 *
 */
public class WiFi {
  //public static volatile boolean recievedParameters = false;
  
  public static int CORNER;
  
  public static Point SPAWN_LL;
  public static Point SPAWN_UR;
  
  public static Point ISLAND_LL;
  public static Point ISLAND_UR;
  
  public static Point TUNNEL_LL;
  public static Point TUNNEL_UR;
  
  public static Point BIN;
  
  /**
   * Method to get team color and corresponding boundary parameters from server and print them out
   */
  public static void wifi() {
    //System.out.println("Map:\n" + wifiParameters);
    //recievedParameters = true;  
    sleepFor(2000); //for for odometer, us, light thread to start
    
    ISLAND_LL = island.ll;
    ISLAND_UR = island.ur;
    
    if (TEAM_NUMBER == greenTeam) {
      CORNER = greenCorner;
      
      SPAWN_LL = green.ll;
      SPAWN_UR = green.ur;
      
      TUNNEL_LL = tng.ll;
      TUNNEL_UR = tng.ur;
      
      BIN = greenBin;
    } 
    else if (TEAM_NUMBER == redTeam) {
      CORNER = redCorner;
      
      SPAWN_LL = red.ll;
      SPAWN_UR = red.ur;
      
      TUNNEL_LL = tnr.ll;
      TUNNEL_UR = tnr.ur;
      
      BIN = redBin;
    }
  }
}