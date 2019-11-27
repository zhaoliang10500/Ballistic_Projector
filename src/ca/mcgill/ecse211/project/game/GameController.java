package ca.mcgill.ecse211.project.game;

//import java.util.ArrayList;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;
//import ca.mcgill.ecse211.project.game.SensorController;
//import ca.mcgill.ecse211.project.sensor.*;
import ca.mcgill.ecse211.project.localization.*;


//import static ca.mcgill.ecse211.project.game.Helper.*;
import static ca.mcgill.ecse211.project.game.Resources.*;

import static ca.mcgill.ecse211.project.game.WifiResources.*;

/**
 * This class runs the entire game
 * It contains the game state machine 
 */
public class GameController {
  
  /**
   * Straightens the robot to face the tunnel entrance 
   * based on the robot's current corner relative to the tunnel
   * @param currCorner
   */
  public static void straighten(int currCorner) {
    if (tunnelOrientation() == 1) { //vertical tunnel
      if (currCorner == 0 || currCorner == 1) {
        Navigation.turnToFace(0);
      }
      else if (currCorner == 2 || currCorner == 3) {
        Navigation.turnToFace(180);
      }
    }
    else if (tunnelOrientation() == 2) { //horizontal tunnel
      if (currCorner == 0 || currCorner == 3) {
        Navigation.turnToFace(90);
        //System.out.println("Facing 90 degrees");
      }
      else if (currCorner == 1 || currCorner == 2) {
        Navigation.turnToFace(270);
      }
    }
  }
  
  /**
   * Method to calculate tunnel orientation
   * @return
   */
  public static int tunnelOrientation() {
    int orientation = -1;
    if (WiFi.TUNNEL_LL.x + 1 == WiFi.TUNNEL_UR.x) { //vertical tunnel
      orientation = 1;
    }
    else if (WiFi.TUNNEL_LL.x + 2 == WiFi.TUNNEL_UR.x) { //horizontal tunnel
      orientation = 2;
    }
    return orientation;
  }
  
  /**
   * Calculates the closest tunnel entrance coordinates
   * @return
   */
  public static Point calcTunnelCoords() {
    double currX = odometer.getXYT()[0];
    double currY = odometer.getXYT()[1];
    
    double deltaX_LL = TILE_SIZE* WiFi.TUNNEL_LL.x - currX;  
    double deltaY_L = TILE_SIZE* WiFi.TUNNEL_LL.y - currY;
    
    double deltaX_LR = TILE_SIZE* (WiFi.TUNNEL_LL.x + 1) - currX;  
    
    double deltaX_UR = TILE_SIZE* WiFi.TUNNEL_UR.x - currX;  
    double deltaY_U = TILE_SIZE* WiFi.TUNNEL_UR.y - currY;
    
    double deltaX_UL = TILE_SIZE* (WiFi.TUNNEL_LL.x -1 )- currX;  
    
    double minDistLL = Math.sqrt(Math.pow(deltaX_LL, 2) + Math.pow(deltaY_L, 2));
    double minDistLR = Math.sqrt(Math.pow(deltaX_LR, 2) + Math.pow(deltaY_L, 2));
    double minDistUR = Math.sqrt(Math.pow(deltaX_UR, 2) + Math.pow(deltaY_U, 2));
    double minDistUL = Math.sqrt(Math.pow(deltaX_UL, 2) + Math.pow(deltaY_U, 2));

    

    Point correctCoords = WiFi.TUNNEL_LL; //just initializing
    // lower tunnel coordinates is closer to robot
    if (minDistLL < minDistLR && minDistLL < minDistUR && minDistLL < minDistUL ||
        minDistLR < minDistLL && minDistLR < minDistUR && minDistLR < minDistUL) {
      correctCoords = WiFi.TUNNEL_LL;
    }
    // upper tunnel coordinates is closer to robot
    else if (minDistUR < minDistLL && minDistUR < minDistLR && minDistUR < minDistUL ||
             minDistUL < minDistLL && minDistUL < minDistLR && minDistLL < minDistUL) {
      correctCoords = WiFi.TUNNEL_UR;
    }
    return correctCoords;
    
  }
  
  /**
   * Calculate the robot's current corner relative to the tunnel
   * @return
   */
  public static int calcCurrCorner() {
    double xBound = TILE_SIZE*(WiFi.TUNNEL_LL.x + WiFi.TUNNEL_UR.x)/2;
    double yBound = TILE_SIZE*(WiFi.TUNNEL_LL.y + WiFi.TUNNEL_UR.y)/2;
    double currX = odometer.getXYT()[0];
    double currY = odometer.getXYT()[1];
    int corner;
    
    if (currX < xBound && currY < yBound) {
      corner = 0;
    }
    else if (currX >= xBound && currY < yBound) {
      corner = 1;
    }
    else if (currX >= xBound && currY >= yBound) {
      corner = 2;
    }
    else if (currX < xBound && currY >= yBound) {
      corner = 3;
    }
    else {
      corner = -1;
    }
    return corner;
  }
  

//  public static double[] calcTravelToTunnel(Point correctCoords, int corner) {
//    double[] travelToTunnel;
//    if (tunnelOrientation() == 1) { //vertical tunnel
//      if (corner == 2 || corner == 3) {
//        travelToTunnel = new double[]{correctCoords.x - 0.5, correctCoords.y + 0.5};
//      }
//      else if (corner == 0 || corner == 1) {
//       travelToTunnel = new double[] {correctCoords.x + 0.5, correctCoords.y - 0.5
//      }
//    }
//    else if (tunnelOrientation() == 2) { //horizontal tunnel
//      if (corner == 0 || corner == 3) {
//        Navigation.travelTo(correctCoords.x - 0.5, correctCoords.y + 0.5, 0, bin);
//      }
//      else if (corner == 1 || corner == 2) {
//        Navigation.travelTo(correctCoords.x + 0.5, correctCoords.y - 0.5, 0, bin);
//      }
//    }
//  }
  
  /**
   * calculates the robot's initial position based on the starting corner
   * @return initial x.y coordinates in integers
   */
  public static double[] calcInitialPos() {
    double initialX = 0;
    double initialY = 0;
    double[] initialPos;;
    
    if (WiFi.CORNER == 0) {
      initialX = 1;
      initialY = 1;
    }
    else if (WiFi.CORNER == 1) {
      initialX = 14;
      initialY = 1;
    }
    else if (WiFi.CORNER == 2) {
      initialX = 14;
      initialY = 8;
    }
    else if (WiFi.CORNER == 3) {
      initialX = 1;
      initialY = 8;
    }
    
    initialPos = new double[]{initialX, initialY};
    return initialPos;
  }
  
  /**
   * Method to beep num times 
   * @param num
   */
  public static void beep(int num) {
    for (int i = 0; i < num; i++) {
      Sound.beep(); 
    }
  }
  
}
