package ca.mcgill.ecse211.project.game;

import java.util.ArrayList;
import lejos.hardware.Sound;

import ca.mcgill.ecse211.project.game.SensorController;
import ca.mcgill.ecse211.project.sensor.*;
import ca.mcgill.ecse211.project.localization.*;
import ca.mcgill.ecse211.project.odometry.*;

import static ca.mcgill.ecse211.project.game.Helper.*;
import static ca.mcgill.ecse211.project.game.Resources.*;
import static ca.mcgill.ecse211.project.localization.Navigation.*;
import static ca.mcgill.ecse211.project.game.WifiResources.*;

/**
 * This class runs the entire game
 * It contains the game state machine 
 */
public class GameController implements Runnable {
  private SensorController sensorCont;
  private USLocalizer USLoc;
  private LightLocalizer lightLoc;
  private LightTunnelLocalizer lightTunnelLoc1, lightTunnelLoc2, lightTunnelLoc3, lightTunnelLoc4;
  private ObstacleAvoidance obAvoid;
  private final int[] testCoords = {4,3};
  public static GameState state;
  public volatile ArrayList<USUser> currUSUsers = new ArrayList<USUser>();
  public volatile ArrayList<LightUser> currLightUsers = new ArrayList<LightUser>();
  
  /**
   * Constructor for the GameController class
   * @param sensorCont The sensor controller
   * @param USLoc The ultrasonic localizer
   * @param lightLoc The light localizer
   * @paran lightTunnelLoc The light localizer for localizing before and after a tunnel
   * @param odoCorrect The odometer correction 
   * @param obAvoid The obstacle avoidance
   */
  public GameController (SensorController sensorCont, USLocalizer USLoc, LightLocalizer lightLoc, 
                         LightTunnelLocalizer lightTunnelLoc1, LightTunnelLocalizer lightTunnelLoc2, 
                         LightTunnelLocalizer lightTunnelLoc3, LightTunnelLocalizer lightTunnelLoc4,
                         ObstacleAvoidance obAvoid) {
    this.sensorCont = sensorCont;
    this.USLoc = USLoc;
    this.lightLoc = lightLoc;
    this.lightTunnelLoc1 = lightTunnelLoc1;
    this.lightTunnelLoc2 = lightTunnelLoc2;
    this.lightTunnelLoc3 = lightTunnelLoc3;
    this.lightTunnelLoc4 = lightTunnelLoc4;
    this.obAvoid = obAvoid;
  }
  
  /**
   * Enum used to define game states
   * Each state turns on/off the ultrasonic poller and the light poller
   *
   */
  public enum GameState {
    US_LOC,
    COLOR_LOC,
    NAVIGATION,
    TUNNEL_LOC1,
    TUNNEL_LOC2,
    TUNNEL_LOC3,
    TUNNEL_LOC4,
    NAV_WITH_OBSTACLE,
    TUNNEL,
    LAUNCH,
    TEST
  }
  
  /**
   * Run method for thread
   */
  @Override
  public void run() {
    startGame();
  }
  
  /**
   * This method runs the logic of the game by changing 
   * the GameState and calling the corresponding methods for each state
   */
  public void startGame() {
    //US localization
    changeState(GameState.US_LOC);
    setLRMotorSpeed(US_SPEED);
    USLoc.localize();
    
    // light localization
    changeState(GameState.COLOR_LOC);
    //setLRMotorSpeed(LS_SPEED);
    lightLoc.localize();
    beep(3);
    
    //Travel to tunnel and face it
    changeState(GameState.NAVIGATION);
    travelToTunnel(calcTunnelCoords(), WiFi.CORNER);
    straighten(WiFi.CORNER);
    
    // light localization before tunnel
    //TODO: tunnel localizer doesn't turn properly...maybe thresholds are wrong?//////////////////////////////////////
    changeState(GameState.TUNNEL_LOC1);
    setLRMotorSpeed(LS_TUNNEL_SPEED);
    lightTunnelLoc1.localize(true); //boolean before = true

    changeState(GameState.TUNNEL);
    setLRMotorSpeed(TUNNEL_SPEED);
    Navigation.travelThroughTunnel();
    
    // light localization after tunnel
    changeState(GameState.TUNNEL_LOC2);
    setLRMotorSpeed(LS_TUNNEL_SPEED);
    lightTunnelLoc2.localize(false); //boolean before = false -> after
    

//    // navigation: travel to launch point
//    changeState(GameState.NAVIGATION);
//    Navigation.travelTo(bin.x - 1.0, bin.y - 1.5, 0);
//    setLRMotorSpeed(NAV_TURN2);
//    Navigation.turnTo(targetAngle); //turn to specified orientation
//    beep(3);
    
    //travel to ideal launch point while avoiding obstacles
    changeState(GameState.NAV_WITH_OBSTACLE);
    obAvoid.travelTo(WiFi.BIN.x, WiFi.BIN.y);
    setLRMotorSpeed(NAV_TURN2);


    beep(3);

    
    // throw balls
    changeState(GameState.LAUNCH);
    for (int i = 0; i<5; i++) {
      Launcher.launch();
    }

    
    sleepFor(25000);
    /////////////////////////////////////////////////////////////////////////
    
    // travel back to tunnel and face it
    changeState(GameState.NAVIGATION);
    int currCorner = calcCurrCorner();
    travelToTunnel(calcTunnelCoords(), currCorner);
    straighten(currCorner);
    
    // light localization before tunnel
    changeState(GameState.TUNNEL_LOC3);
    setLRMotorSpeed(LS_TUNNEL_SPEED);
    lightTunnelLoc3.localize(true); //boolean before = true

    changeState(GameState.TUNNEL);
    setLRMotorSpeed(TUNNEL_SPEED);
    Navigation.travelThroughTunnel();
    
    // light localization after tunnel
    changeState(GameState.TUNNEL_LOC4);
    setLRMotorSpeed(LS_TUNNEL_SPEED);
    lightTunnelLoc4.localize(false); //boolean before = false -> after
    
    // travel back to starting position
    changeState(GameState.NAVIGATION);
    double[] initialXY = calcInitialPos();
    //TODO: this keeps moving, not right ////////////////////////////////////////////
    Navigation.travelTo(initialXY[0], initialXY[1], 0, false);
    beep(5);

    
  }
  
  /**
   * Straightens the robot to face the tunnel entrance 
   * based on the robot's current corner relative to the tunnel
   * @param currCorner
   */
  public void straighten(int currCorner) {
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
  public int tunnelOrientation() {
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
  public Point calcTunnelCoords() {
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
   * Calculated the robot's current corner relative to the tunnel
   * @return
   */
  public int calcCurrCorner() {
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
    
    int currCorner = 0; //just initializing
    // lower tunnel coordinates is closer to robot
    if (minDistLL < minDistLR && minDistLL < minDistUR && minDistLL < minDistUL) {
      currCorner = 0;
    }
    else if (minDistLR < minDistLL && minDistLR < minDistUR && minDistLR < minDistUL) { 
      currCorner = 1;
    }
    
    // upper tunnel coordinates is closer to robot
    else if (minDistUR < minDistLL && minDistUR < minDistLR && minDistUR < minDistUL ) {
      currCorner = 2;
    }
    else if (minDistUL < minDistLL && minDistUL < minDistLR && minDistLL < minDistUL) {
      currCorner = 3;
    }
    return currCorner;
    
  }
  
  /**
   * Method to travel to a tunnel, vertical or horizontal
   * @param correctCoords
   */
  public void travelToTunnel(Point correctCoords, int corner){
    if (tunnelOrientation() == 1) { //vertical tunnel
      if (corner == 2 || corner == 3) {
        Navigation.travelTo(correctCoords.x - 0.5, correctCoords.y + 0.5, 0, false);
      }
      else if (corner == 0 || corner == 1) {
        Navigation.travelTo(correctCoords.x + 0.5, correctCoords.y - 0.5, 0, false);
      }
    }
    else if (tunnelOrientation() == 2) { //horizontal tunnel
      if (corner == 0 || corner == 3) {
        Navigation.travelTo(correctCoords.x - 0.5, correctCoords.y + 0.5, 0, false);
      }
      else if (corner == 1 || corner == 2) {
        Navigation.travelTo(correctCoords.x + 0.5, correctCoords.y - 0.5, 0, false);
      }
    }
  }

  /**
   * calculates the robot's initial position based on the starting corner
   * @return
   */
  public double[] calcInitialPos() {
    double initialX = 0;
    double initialY = 0;
    double[] initialPos;;
    
    if (WiFi.CORNER == 0) {
      initialX = TILE_SIZE;
      initialY = TILE_SIZE;
    }
    else if (WiFi.CORNER == 1) {
      initialX = 14*TILE_SIZE;
      initialY = TILE_SIZE;
    }
    else if (WiFi.CORNER == 2) {
      initialX = 14*TILE_SIZE;
      initialY = 8*TILE_SIZE;
    }
    else if (WiFi.CORNER == 3) {
      initialX = TILE_SIZE;
      initialY = 8*TILE_SIZE;
    }
    
    initialPos = new double[]{initialX, initialY};
    return initialPos;
  }
  
  /**
   * Method to beep num times 
   * @param num
   */
  public void beep(int num) {
    for (int i = 0; i < num; i++) {
      Sound.beep(); 
    }
  }
  
  /**
   * Method that defines which threads are running for every state of the game
   * @param newState GameState enum 
   */
  public void changeState(GameState newState) {
    state = newState;
    
    switch (state) { 
      case US_LOC:
        currUSUsers.add(USLoc);
        sensorCont.resumeUSPoller();
        sensorCont.pauseLightPoller();
        break;
        
      case COLOR_LOC:
        currLightUsers.add(lightLoc);
        sensorCont.pauseUSPoller();
        sensorCont.resumeLightPoller();
        break;
        
      case NAVIGATION:
        currUSUsers.remove(USLoc);
        currLightUsers.remove(lightLoc);
        sensorCont.pauseUSPoller();
        sensorCont.pauseLightPoller();
        break;
        
      case TUNNEL_LOC1:
        currLightUsers.add(lightTunnelLoc1);
        sensorCont.pauseUSPoller();
        sensorCont.resumeLightPoller();
        break;
      
      case TUNNEL_LOC2:
        currLightUsers.remove(lightTunnelLoc1);
        currLightUsers.add(lightTunnelLoc2);
        sensorCont.pauseUSPoller();
        sensorCont.resumeLightPoller();
        break;
      
      case TUNNEL_LOC3:
        currLightUsers.remove(lightTunnelLoc2);
        currLightUsers.add(lightTunnelLoc3);
        sensorCont.pauseUSPoller();
        sensorCont.resumeLightPoller();
        break;
        
      case TUNNEL_LOC4:
        currLightUsers.remove(lightTunnelLoc3);
        currLightUsers.add(lightTunnelLoc4);
        sensorCont.pauseUSPoller();
        sensorCont.resumeLightPoller();
        break;
        
      case NAV_WITH_OBSTACLE:
        currUSUsers.add(obAvoid);
        sensorCont.resumeUSPoller();
        sensorCont.pauseLightPoller();
        break;

      case TUNNEL:
        sensorCont.pauseUSPoller();
        sensorCont.pauseLightPoller();
        break;
        
      case LAUNCH:
        sensorCont.pauseUSPoller();
        sensorCont.pauseLightPoller();
        break;
        
      case TEST:
        //TODO: change this as necessary for any test
        sensorCont.resumeUSPoller();
        sensorCont.resumeLightPoller();
        break;
    }
    sensorCont.setCurrUSUsers(currUSUsers);
    sensorCont.setCurrLightUsers(currLightUsers);
  }
}
